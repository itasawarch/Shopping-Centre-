# Zam Zam POS - Complete Production Deployment & Build Configurations

This document provides complete, production-ready build configurations and deployment instructions for **Zam Zam POS**, spanning **Android Native**, **Electron Desktop**, and **Web-first offline sandboxes**.

---

## 1. Android Build Configuration & Native Compilation

### 1.1 Local JVM Compilation & Testing
To compile and test the Android application using local JVM processes:
```bash
# Verify the syntax and compile the APK without executing tests
gradle assembleDebug

# Execute all Robolectric unit and behavioral tests
gradle :app:testDebugUnitTest
```

### 1.2 Production APK & AAB Compilation
To generate highly optimized, signed binaries ready for Google Play Store or private MDM deployments:
```bash
# Clean previous build artifacts
gradle clean

# Generate the Release App Bundle (AAB) for Play Store deployment
gradle :app:bundleRelease

# Generate the direct installable Release APK
gradle :app:assembleRelease
```
The output binaries will be compiled to:
- **APK:** `app/build/outputs/apk/release/app-release.apk`
- **AAB:** `app/build/outputs/bundle/release/app-release.aab`

### 1.3 Recommended ProGuard Rules (`app/proguard-rules.pro`)
Add the following rules to protect your Room Database mapping definitions and prevent class-stripping of SQLite model schemas:
```proguard
# Keep Room Entities and DAOs
-keepclassmembers class * {
    @androidx.room.Database *;
    @androidx.room.Dao *;
    @androidx.room.Entity *;
}
-keep class * extends androidx.room.RoomDatabase
-keep class * implements androidx.room.POSDao

# Retain serialization properties for local transactions
-keepclassmembers class com.example.data.** { *; }
```

---

## 2. Electron Desktop Build Configuration

Since **Zam Zam POS** uses local IndexedDB & SQLite schemas for robust offline-first persistence across user sessions, you can wrap and bundle it into a **Native Desktop App (Windows, macOS, Linux)** using Electron. 

Below are the **full, complete configuration files** needed to run Zam Zam POS as an Electron desktop application.

### 2.1 Electron Configuration: `package.json`
Create a directory named `desktop-wrapper` and add the following production `package.json` file:

```json
{
  "name": "zam-zam-pos-desktop",
  "version": "1.0.0",
  "description": "Enterprise Offline-First POS Desktop Application for Zam Zam Whole Sale",
  "main": "main.js",
  "scripts": {
    "start": "electron .",
    "build:win": "electron-builder --win portable",
    "build:mac": "electron-builder --mac",
    "build:linux": "electron-builder --linux deb"
  },
  "author": "Zam Zam POS Engineering Team",
  "license": "Proprietary",
  "dependencies": {
    "electron-is-dev": "^2.0.0"
  },
  "devDependencies": {
    "electron": "^25.3.0",
    "electron-builder": "^24.4.0"
  },
  "build": {
    "appId": "com.zamzam.pos.desktop",
    "productName": "ZamZamPOS",
    "files": [
      "main.js",
      "dist/**/*"
    ],
    "directories": {
      "output": "dist-desktop"
    },
    "win": {
      "target": "portable",
      "icon": "assets/icon.ico"
    },
    "mac": {
      "category": "public.app-category.business",
      "target": "dmg"
    },
    "linux": {
      "target": "deb",
      "category": "Office"
    }
  }
}
```

### 2.2 Electron Main Thread: `main.js`
Create the `main.js` entry file inside the `desktop-wrapper` folder. This configures safe inter-process communication (IPC), hardware accelerated canvas support, local file buffers, and Chromium print capabilities for offline physical receipt printing:

```javascript
const { app, BrowserWindow, ipcMain, dialog } = require('electron');
const path = require('path');
const isDev = require('electron-is-dev');

let mainWindow;

function createMainWindow() {
  mainWindow = new BrowserWindow({
    title: "Zam Zam POS - Enterprise Terminal",
    width: 1280,
    height: 800,
    minWidth: 1024,
    minHeight: 768,
    show: false,
    backgroundColor: '#FFFFFF',
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      sandbox: true,
      preload: path.join(__dirname, 'preload.js')
    }
  });

  // Load the web application containing our IndexedDB persistence layer
  if (isDev) {
    mainWindow.loadURL('http://localhost:3000');
    mainWindow.webContents.openDevTools();
  } else {
    mainWindow.loadFile(path.join(__dirname, 'dist/index.html'));
  }

  mainWindow.once('ready-to-show', () => {
    mainWindow.maximize();
    mainWindow.show();
  });

  mainWindow.on('closed', () => {
    mainWindow = null;
  });
}

app.whenReady().then(() => {
  createMainWindow();

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createMainWindow();
  });
});

// Handle graceful terminations
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit();
});

// Native Print Interface for Thermal Receipt Roll Printers
ipcMain.handle('print-receipt', async (event, htmlContent) => {
  let printWindow = new BrowserWindow({ show: false, webPreferences: { nodeIntegration: true } });
  printWindow.loadURL("data:text/html;charset=utf-8," + encodeURIComponent(htmlContent));

  printWindow.webContents.on('did-finish-load', () => {
    printWindow.webContents.print({
      silent: true,
      printBackground: true,
      deviceName: '', // Default OS printer or defined thermal receipt device
      margins: { marginType: 'none' },
      pageSize: { width: 80000, height: 200000 } // Thermal paper size 80mm roll standard
    }, (success, failureReason) => {
      printWindow.close();
    });
  });
});
```

---

## 3. Deployment Instructions

### 3.1 Web & Offline PWA Hosting
Since **Zam Zam POS** uses IndexedDB for database persistence and client-side processing, it can be hosted on high-performance Static Site hosting architectures (Google Firebase Hosting, Netlify, Vercel, or AWS Amplify):
1. Compile the web bundle using your compiler toolchain: `npm run build`.
2. Deploy the `dist` or `build` folders to Firebase Hosting:
   ```bash
   firebase login
   firebase init hosting
   firebase deploy --only hosting
   ```
3. Users visiting the URL will download a self-contained progressive web app (PWA) that installs locally and operates fully offline without any server roundtrips.

### 3.2 Desktop Packing Guide
To build and pack the standalone desktop installer:
1. Compile your production bundle: `npm run build`.
2. Move the output files into the `desktop-wrapper/dist/` directory.
3. Run:
   ```bash
   cd desktop-wrapper
   npm install
   npm run build:win   # For Windows portable EXE
   npm run build:linux # For Linux Debian installation
   ```
4. Find the production installer in `desktop-wrapper/dist-desktop/`.
