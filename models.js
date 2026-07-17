/**
 * ZamZam ERP - Core Models Module (models.js)
 * 
 * Standardizes data definitions, validations, and mapping helpers across the application.
 * Defines JavaScript classes for Products, Customers, and Sales, ensuring safe conversion 
 * between SQLite database rows, program instances, and Firestore-compatible JSON payloads.
 */

/**
 * Represents a Product in the inventory system.
 */
class Product {
    /**
     * @param {Object} data - Plain object data
     */
    constructor(data = {}) {
        this.id = data.id || null;
        this.name = data.name ? data.name.trim() : '';
        this.sku = data.sku ? data.sku.trim().toUpperCase() : '';
        this.barcode = data.barcode ? data.barcode.trim() : null;
        this.category = data.category ? data.category.trim() : 'General';
        this.brand = data.brand ? data.brand.trim() : null;
        this.unit = data.unit ? data.unit.trim() : 'Pcs';
        this.purchasePrice = data.purchasePrice !== undefined ? Number(data.purchasePrice) : 0.0;
        this.retailPrice = data.retailPrice !== undefined ? Number(data.retailPrice) : 0.0;
        this.wholesalePrice = data.wholesalePrice !== undefined ? Number(data.wholesalePrice) : 0.0;
        this.stockQuantity = data.stockQuantity !== undefined ? parseInt(data.stockQuantity, 10) : 0;
        this.minStockAlert = data.minStockAlert !== undefined ? parseInt(data.minStockAlert, 10) : 0;
        this.expiryDate = data.expiryDate || null;
        this.imagePath = data.imagePath || null;
        this.isSynced = data.isSynced !== undefined ? (data.isSynced ? 1 : 0) : 0;
        this.lastUpdated = data.lastUpdated ? Number(data.lastUpdated) : Date.now();
    }

    /**
     * Creates an instance of Product from a SQLite database row.
     * @param {Object} row 
     * @returns {Product|null}
     */
    static fromRow(row) {
        if (!row) return null;
        return new Product(row);
    }

    /**
     * Validates the core product constraints.
     * @throws {Error} If fields are invalid.
     */
    validate() {
        if (!this.name || this.name.trim() === '') {
            throw new Error('Product name is required.');
        }
        if (!this.sku || this.sku.trim() === '') {
            throw new Error('Product SKU is required.');
        }
        if (isNaN(this.purchasePrice) || this.purchasePrice < 0) {
            throw new Error('Purchase price must be a non-negative number.');
        }
        if (isNaN(this.retailPrice) || this.retailPrice < 0) {
            throw new Error('Retail price must be a non-negative number.');
        }
        if (isNaN(this.wholesalePrice) || this.wholesalePrice < 0) {
            throw new Error('Wholesale price must be a non-negative number.');
        }
        if (isNaN(this.stockQuantity) || this.stockQuantity < 0) {
            throw new Error('Stock quantity must be a non-negative integer.');
        }
        if (isNaN(this.minStockAlert) || this.minStockAlert < 0) {
            throw new Error('Minimum stock alert must be a non-negative integer.');
        }
    }

    /**
     * Maps class attributes to a flat database row format suitable for SQLite queries.
     * @returns {Object} Plain database-friendly object.
     */
    toRow() {
        return {
            id: this.id,
            name: this.name,
            sku: this.sku,
            barcode: this.barcode,
            category: this.category,
            brand: this.brand,
            unit: this.unit,
            purchasePrice: this.purchasePrice,
            retailPrice: this.retailPrice,
            wholesalePrice: this.wholesalePrice,
            stockQuantity: this.stockQuantity,
            minStockAlert: this.minStockAlert,
            expiryDate: this.expiryDate,
            imagePath: this.imagePath,
            isSynced: this.isSynced,
            lastUpdated: this.lastUpdated
        };
    }

    /**
     * Prepares a clean JSON object for pushing to Firestore (excludes metadata sync fields).
     * @returns {Object} JSON payload.
     */
    toFirestore() {
        return {
            id: this.id,
            name: this.name,
            sku: this.sku,
            barcode: this.barcode,
            category: this.category,
            brand: this.brand,
            unit: this.unit,
            purchasePrice: this.purchasePrice,
            retailPrice: this.retailPrice,
            wholesalePrice: this.wholesalePrice,
            stockQuantity: this.stockQuantity,
            minStockAlert: this.minStockAlert,
            expiryDate: this.expiryDate,
            imagePath: this.imagePath,
            lastUpdated: this.lastUpdated
        };
    }
}

/**
 * Represents a Customer in the system.
 */
class Customer {
    /**
     * @param {Object} data - Plain object data
     */
    constructor(data = {}) {
        this.id = data.id || null;
        this.name = data.name ? data.name.trim() : '';
        this.phone = data.phone ? data.phone.trim() : '';
        this.email = data.email ? data.email.trim() : null;
        this.address = data.address ? data.address.trim() : null;
        this.balance = data.balance !== undefined ? Number(data.balance) : 0.0;
        this.isSynced = data.isSynced !== undefined ? (data.isSynced ? 1 : 0) : 0;
        this.lastUpdated = data.lastUpdated ? Number(data.lastUpdated) : Date.now();
    }

    /**
     * Creates an instance of Customer from a SQLite database row.
     * @param {Object} row 
     * @returns {Customer|null}
     */
    static fromRow(row) {
        if (!row) return null;
        return new Customer(row);
    }

    /**
     * Validates the core customer constraints.
     * @throws {Error} If fields are invalid.
     */
    validate() {
        if (!this.name || this.name.trim() === '') {
            throw new Error('Customer name is required.');
        }
        if (!this.phone || this.phone.trim() === '') {
            throw new Error('Customer phone number is required.');
        }
        if (isNaN(this.balance)) {
            throw new Error('Customer balance must be a valid number.');
        }
    }

    /**
     * Maps class attributes to a flat database row format suitable for SQLite queries.
     * @returns {Object} Plain database-friendly object.
     */
    toRow() {
        return {
            id: this.id,
            name: this.name,
            phone: this.phone,
            email: this.email,
            address: this.address,
            balance: this.balance,
            isSynced: this.isSynced,
            lastUpdated: this.lastUpdated
        };
    }

    /**
     * Prepares a clean JSON object for pushing to Firestore (excludes metadata sync fields).
     * @returns {Object} JSON payload.
     */
    toFirestore() {
        return {
            id: this.id,
            name: this.name,
            phone: this.phone,
            email: this.email,
            address: this.address,
            balance: this.balance,
            lastUpdated: this.lastUpdated
        };
    }
}

/**
 * Represents a Sale transaction in the POS.
 */
class Sale {
    /**
     * @param {Object} data - Plain object data
     */
    constructor(data = {}) {
        this.id = data.id || null;
        this.timestamp = data.timestamp ? Number(data.timestamp) : Date.now();
        this.customerId = data.customerId || 'GUEST';
        this.customerName = data.customerName || 'Guest Customer';
        this.subtotal = data.subtotal !== undefined ? Number(data.subtotal) : 0.0;
        this.discount = data.discount !== undefined ? Number(data.discount) : 0.0;
        this.tax = data.tax !== undefined ? Number(data.tax) : 0.0;
        this.totalAmount = data.totalAmount !== undefined ? Number(data.totalAmount) : 0.0;
        this.paymentMethod = data.paymentMethod ? data.paymentMethod.trim() : 'Cash';
        this.cashierName = data.cashierName ? data.cashierName.trim() : 'Unknown';
        this.status = data.status || 'Completed';
        this.isSynced = data.isSynced !== undefined ? (data.isSynced ? 1 : 0) : 0;
    }

    /**
     * Creates an instance of Sale from a SQLite database row.
     * @param {Object} row 
     * @returns {Sale|null}
     */
    static fromRow(row) {
        if (!row) return null;
        return new Sale(row);
    }

    /**
     * Validates the core sales transaction details.
     * @throws {Error} If fields are invalid.
     */
    validate() {
        if (isNaN(this.subtotal) || this.subtotal < 0) {
            throw new Error('Sale subtotal must be a non-negative number.');
        }
        if (isNaN(this.discount) || this.discount < 0) {
            throw new Error('Sale discount must be a non-negative number.');
        }
        if (isNaN(this.tax) || this.tax < 0) {
            throw new Error('Sale tax must be a non-negative number.');
        }
        if (isNaN(this.totalAmount) || this.totalAmount < 0) {
            throw new Error('Sale total amount must be a non-negative number.');
        }
        if (!this.paymentMethod || this.paymentMethod.trim() === '') {
            throw new Error('Payment method is required.');
        }
        if (!this.cashierName || this.cashierName.trim() === '') {
            throw new Error('Cashier name is required.');
        }
    }

    /**
     * Maps class attributes to a flat database row format suitable for SQLite queries.
     * @returns {Object} Plain database-friendly object.
     */
    toRow() {
        return {
            id: this.id,
            timestamp: this.timestamp,
            customerId: this.customerId,
            customerName: this.customerName,
            subtotal: this.subtotal,
            discount: this.discount,
            tax: this.tax,
            totalAmount: this.totalAmount,
            paymentMethod: this.paymentMethod,
            cashierName: this.cashierName,
            status: this.status,
            isSynced: this.isSynced
        };
    }

    /**
     * Prepares a clean JSON object for pushing to Firestore (excludes metadata sync fields).
     * @returns {Object} JSON payload.
     */
    toFirestore() {
        return {
            id: this.id,
            timestamp: this.timestamp,
            customerId: this.customerId,
            customerName: this.customerName,
            subtotal: this.subtotal,
            discount: this.discount,
            tax: this.tax,
            totalAmount: this.totalAmount,
            paymentMethod: this.paymentMethod,
            cashierName: this.cashierName,
            status: this.status
        };
    }
}

module.exports = {
    Product,
    Customer,
    Sale
};
