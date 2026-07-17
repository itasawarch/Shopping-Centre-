/**
 * ZamZam ERP - 'Product Service' Core Module (productService.js)
 * 
 * High-performance, secure product database service interfacing with 'better-sqlite3'
 * database to manage local inventory.
 * 
 * This service ensures that all creates, updates, and stock adjustments mark the local
 * records as unsynced (isSynced = 0) and updates the 'lastUpdated' timestamp. This enables
 * the Sync Manager to detect and propagate offline changes to Cloud Firestore.
 */

const { db } = require('./betterSqlite3Config');
const crypto = require('crypto');

class ProductService {
    /**
     * Helper to generate a secure unique identifier for products
     * @returns {string} UUID v4
     */
    static generateId() {
        return crypto.randomUUID ? crypto.randomUUID() : crypto.randomBytes(16).toString('hex');
    }

    /**
     * Validates product fields before insertion or update
     * @param {Object} productData 
     * @param {boolean} isUpdate 
     */
    static validateProduct(productData, isUpdate = false) {
        if (!isUpdate) {
            if (!productData.name || typeof productData.name !== 'string' || productData.name.trim() === '') {
                throw new Error("Product name is required and must be a valid string.");
            }
            if (!productData.sku || typeof productData.sku !== 'string' || productData.sku.trim() === '') {
                throw new Error("Product SKU is required and must be a valid string.");
            }
        } else {
            if (productData.name !== undefined && (typeof productData.name !== 'string' || productData.name.trim() === '')) {
                throw new Error("Updated product name cannot be empty.");
            }
            if (productData.sku !== undefined && (typeof productData.sku !== 'string' || productData.sku.trim() === '')) {
                throw new Error("Updated product SKU cannot be empty.");
            }
        }

        // Validate prices & quantities if provided
        const numericFields = [
            { key: 'purchasePrice', label: 'Purchase Price' },
            { key: 'retailPrice', label: 'Retail Price' },
            { key: 'wholesalePrice', label: 'Wholesale Price' },
            { key: 'stockQuantity', label: 'Stock Quantity' },
            { key: 'minStockAlert', label: 'Minimum Stock Alert' }
        ];

        numericFields.forEach(({ key, label }) => {
            if (productData[key] !== undefined && productData[key] !== null) {
                const val = Number(productData[key]);
                if (isNaN(val) || val < 0) {
                    throw new Error(`${label} must be a non-negative number.`);
                }
            }
        });
    }

    /**
     * Creates a new product in the local SQLite database
     * Automatically sets 'isSynced' to 0 and 'lastUpdated' to the current epoch time.
     * 
     * @param {Object} productData 
     * @returns {Promise<Object>} The newly created product record
     */
    async create(productData) {
        console.log(`[ProductService] Creating product with SKU: ${productData.sku}`);
        
        // 1. Standard Input Validation
        ProductService.validateProduct(productData);

        const id = productData.id || ProductService.generateId();
        const name = productData.name.trim();
        const sku = productData.sku.trim().toUpperCase();
        const barcode = productData.barcode ? productData.barcode.trim() : null;
        const category = productData.category ? productData.category.trim() : 'General';
        const brand = productData.brand ? productData.brand.trim() : null;
        const unit = productData.unit ? productData.unit.trim() : 'Pcs';
        const purchasePrice = Number(productData.purchasePrice) || 0.0;
        const retailPrice = Number(productData.retailPrice) || 0.0;
        const wholesalePrice = Number(productData.wholesalePrice) || 0.0;
        const stockQuantity = parseInt(productData.stockQuantity, 10) || 0;
        const minStockAlert = parseInt(productData.minStockAlert, 10) || 0;
        const expiryDate = productData.expiryDate || null;
        const imagePath = productData.imagePath || null;
        const isSynced = 0; // Set to unsynced so the Sync Manager uploads it to cloud
        const lastUpdated = Date.now();

        try {
            // 2. Check for unique SKU conflict beforehand to provide clean error handling
            const existingSku = db.prepare('SELECT id FROM products WHERE sku = ? LIMIT 1').get(sku);
            if (existingSku) {
                throw new Error(`Product SKU "${sku}" is already in use by another product.`);
            }

            // 3. Check for barcode conflict if barcode is provided
            if (barcode) {
                const existingBarcode = db.prepare('SELECT id FROM products WHERE barcode = ? LIMIT 1').get(barcode);
                if (existingBarcode) {
                    throw new Error(`Product barcode "${barcode}" is already assigned to another product.`);
                }
            }

            // 4. Insert into SQLite
            const stmt = db.prepare(`
                INSERT INTO products (
                    id, name, sku, barcode, category, brand, unit,
                    purchasePrice, retailPrice, wholesalePrice, stockQuantity,
                    minStockAlert, expiryDate, imagePath, isSynced, lastUpdated
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            `);

            stmt.run(
                id, name, sku, barcode, category, brand, unit,
                purchasePrice, retailPrice, wholesalePrice, stockQuantity,
                minStockAlert, expiryDate, imagePath, isSynced, lastUpdated
            );

            console.log(`[ProductService] Product created successfully: ${name} (ID: ${id})`);
            return this.getById(id);
        } catch (error) {
            console.error(`[ProductService] Error creating product:`, error);
            throw error;
        }
    }

    /**
     * Retrieves a single product by its unique database ID
     * @param {string} id 
     * @returns {Promise<Object|null>} Product record or null
     */
    async getById(id) {
        if (!id) return null;
        try {
            const product = db.prepare('SELECT * FROM products WHERE id = ?').get(id);
            return product || null;
        } catch (error) {
            console.error(`[ProductService] Error fetching product by ID (${id}):`, error);
            throw error;
        }
    }

    /**
     * Retrieves a single product by its unique SKU (Stock Keeping Unit)
     * @param {string} sku 
     * @returns {Promise<Object|null>} Product record or null
     */
    async getBySku(sku) {
        if (!sku) return null;
        try {
            const product = db.prepare('SELECT * FROM products WHERE sku = ?').get(sku.trim().toUpperCase());
            return product || null;
        } catch (error) {
            console.error(`[ProductService] Error fetching product by SKU (${sku}):`, error);
            throw error;
        }
    }

    /**
     * Retrieves a product by its barcode lookup
     * @param {string} barcode 
     * @returns {Promise<Object|null>} Product record or null
     */
    async getByBarcode(barcode) {
        if (!barcode) return null;
        try {
            const product = db.prepare('SELECT * FROM products WHERE barcode = ?').get(barcode.trim());
            return product || null;
        } catch (error) {
            console.error(`[ProductService] Error fetching product by Barcode (${barcode}):`, error);
            throw error;
        }
    }

    /**
     * Retrieves, searches, and paginates products in the inventory database
     * Supports highly flexible search, category filtering, and low stock warnings.
     * 
     * @param {Object} options 
     * @param {string} [options.search] - Search text (matches name, sku, or barcode)
     * @param {string} [options.category] - Exact category filter
     * @param {string} [options.brand] - Exact brand filter
     * @param {boolean} [options.lowStock] - Filter only products under minimum stock alert threshold
     * @param {string} [options.sortBy='name'] - Sorting column
     * @param {string} [options.sortOrder='ASC'] - Sort direction ('ASC' or 'DESC')
     * @param {number} [options.limit] - Max number of records to return
     * @param {number} [options.offset=0] - Offset for pagination
     * @returns {Promise<{products: Array<Object>, totalCount: number}>} Paginated payload
     */
    async getAll(options = {}) {
        try {
            const {
                search,
                category,
                brand,
                lowStock,
                sortBy = 'name',
                sortOrder = 'ASC',
                limit,
                offset = 0
            } = options;

            // Build dynamic query clauses
            const whereClauses = [];
            const params = [];

            if (search) {
                whereClauses.push('(name LIKE ? OR sku LIKE ? OR barcode LIKE ?)');
                const searchTerm = `%${search.trim()}%`;
                params.push(searchTerm, searchTerm, searchTerm);
            }

            if (category) {
                whereClauses.push('category = ?');
                params.push(category.trim());
            }

            if (brand) {
                whereClauses.push('brand = ?');
                params.push(brand.trim());
            }

            if (lowStock === true || lowStock === 'true') {
                whereClauses.push('stockQuantity <= minStockAlert');
            }

            const whereSql = whereClauses.length > 0 ? `WHERE ${whereClauses.join(' AND ')}` : '';
            
            // Whitelist sort column to prevent SQL injection
            const allowedSortColumns = [
                'id', 'name', 'sku', 'barcode', 'category', 'brand', 
                'purchasePrice', 'retailPrice', 'wholesalePrice', 
                'stockQuantity', 'minStockAlert', 'lastUpdated'
            ];
            const safeSortBy = allowedSortColumns.includes(sortBy) ? sortBy : 'name';
            const safeSortOrder = sortOrder.toUpperCase() === 'DESC' ? 'DESC' : 'ASC';

            // Get total count matching criteria
            const countSql = `SELECT COUNT(*) as count FROM products ${whereSql}`;
            const countResult = db.prepare(countSql).get(...params);
            const totalCount = countResult ? countResult.count : 0;

            // Retrieve products
            let querySql = `SELECT * FROM products ${whereSql} ORDER BY ${safeSortBy} ${safeSortOrder}`;
            if (limit !== undefined && limit !== null) {
                querySql += ` LIMIT ? OFFSET ?`;
                params.push(parseInt(limit, 10), parseInt(offset, 10));
            }

            const products = db.prepare(querySql).all(...params);
            return {
                products,
                totalCount
            };
        } catch (error) {
            console.error('[ProductService] Failed to retrieve products list:', error);
            throw error;
        }
    }

    /**
     * Updates an existing product record locally in SQLite
     * Resets the 'isSynced' status to 0 and updates 'lastUpdated' to mark it dirty.
     * 
     * @param {string} id - Product ID
     * @param {Object} updateData - Product fields to modify
     * @returns {Promise<Object>} The updated product record
     */
    async update(id, updateData) {
        console.log(`[ProductService] Attempting update for product ID: ${id}`);
        if (!id) {
            throw new Error("Product ID is required for updating.");
        }

        // 1. Verify product exists
        const existingProduct = await this.getById(id);
        if (!existingProduct) {
            throw new Error(`Product with ID "${id}" does not exist.`);
        }

        // 2. Validate input fields
        ProductService.validateProduct(updateData, true);

        // 3. Handle SKU uniqueness check if SKU is changing
        if (updateData.sku) {
            const updatedSku = updateData.sku.trim().toUpperCase();
            if (updatedSku !== existingProduct.sku) {
                const skuConflict = db.prepare('SELECT id FROM products WHERE sku = ? AND id != ? LIMIT 1').get(updatedSku, id);
                if (skuConflict) {
                    throw new Error(`Product SKU "${updatedSku}" is already in use by another product.`);
                }
            }
        }

        // 4. Handle barcode uniqueness check if barcode is changing
        if (updateData.barcode) {
            const updatedBarcode = updateData.barcode.trim();
            if (updatedBarcode !== existingProduct.barcode) {
                const barcodeConflict = db.prepare('SELECT id FROM products WHERE barcode = ? AND id != ? LIMIT 1').get(updatedBarcode, id);
                if (barcodeConflict) {
                    throw new Error(`Product barcode "${updatedBarcode}" is already in use by another product.`);
                }
            }
        }

        // 5. Dynamically compile updates list to prevent overwriting omitted fields
        const updatableFields = [
            'name', 'sku', 'barcode', 'category', 'brand', 'unit',
            'purchasePrice', 'retailPrice', 'wholesalePrice', 
            'stockQuantity', 'minStockAlert', 'expiryDate', 'imagePath'
        ];

        const setStatements = [];
        const params = [];

        updatableFields.forEach(field => {
            if (updateData[field] !== undefined) {
                setStatements.push(`${field} = ?`);
                
                let val = updateData[field];
                if (field === 'sku') val = val.trim().toUpperCase();
                else if (field === 'name') val = val.trim();
                else if (['purchasePrice', 'retailPrice', 'wholesalePrice'].includes(field)) val = Number(val);
                else if (['stockQuantity', 'minStockAlert'].includes(field)) val = parseInt(val, 10);
                
                params.push(val);
            }
        });

        // If no updatable fields provided, return the current product details directly
        if (setStatements.length === 0) {
            console.log('[ProductService] No updatable fields supplied. Skipping DB write.');
            return existingProduct;
        }

        // Automatically set sync parameters to flag this as modified locally
        setStatements.push('isSynced = 0');
        setStatements.push('lastUpdated = ?');
        params.push(Date.now());

        // Append the product ID for the WHERE clause
        params.push(id);

        const sql = `UPDATE products SET ${setStatements.join(', ')} WHERE id = ?`;

        try {
            const stmt = db.prepare(sql);
            const result = stmt.run(...params);
            
            if (result.changes === 0) {
                throw new Error("Update completed but 0 records were modified.");
            }

            console.log(`[ProductService] Product ID ${id} updated successfully.`);
            return this.getById(id);
        } catch (error) {
            console.error(`[ProductService] Failed to update product ID ${id}:`, error);
            throw error;
        }
    }

    /**
     * Physically deletes a product from the local database
     * Note: Deletions are physical locally. In full cloud-sync scenarios, 
     * a soft delete or tombstone queue is recommended to sync deletions.
     * 
     * @param {string} id 
     * @returns {Promise<boolean>} True if deleted successfully
     */
    async delete(id) {
        console.log(`[ProductService] Deleting product ID: ${id}`);
        if (!id) {
            throw new Error("Product ID is required for deletion.");
        }

        try {
            const stmt = db.prepare('DELETE FROM products WHERE id = ?');
            const result = stmt.run(id);
            
            if (result.changes === 0) {
                throw new Error(`Product with ID "${id}" could not be deleted or does not exist.`);
            }

            console.log(`[ProductService] Product ID ${id} deleted successfully from SQLite.`);
            return true;
        } catch (error) {
            console.error(`[ProductService] Error deleting product ID ${id}:`, error);
            throw error;
        }
    }

    /**
     * Adjusts the stock quantities for multiple products inside a database transaction.
     * This atomic method ensures that either all inventory adjustments are recorded, or none.
     * Marks all adjusted products as unsynced (isSynced = 0) and sets 'lastUpdated'.
     * 
     * @param {Array<{id: string, quantityAdjusted: number}>} adjustments 
     * @returns {Promise<boolean>} True if all adjustments completed successfully
     */
    async bulkAdjustStock(adjustments) {
        console.log(`[ProductService] Running bulk stock adjustments for ${adjustments.length} items.`);
        if (!Array.isArray(adjustments) || adjustments.length === 0) {
            throw new Error("Invalid or empty adjustments array.");
        }

        // Run entire bulk operation inside an isolated, safe transaction
        const adjustTransaction = db.transaction(() => {
            const updateStmt = db.prepare(`
                UPDATE products 
                SET stockQuantity = stockQuantity + ?,
                    isSynced = 0,
                    lastUpdated = ? 
                WHERE id = ?
            `);

            for (const adj of adjustments) {
                const { id, quantityAdjusted } = adj;
                if (!id) {
                    throw new Error("Product ID is required for stock adjustments.");
                }

                const changeAmount = parseInt(quantityAdjusted, 10);
                if (isNaN(changeAmount)) {
                    throw new Error(`Invalid stock adjustment amount for product ID: ${id}`);
                }

                // Verify the product exists and the adjustment won't make stock negative
                const product = db.prepare('SELECT name, stockQuantity FROM products WHERE id = ?').get(id);
                if (!product) {
                    throw new Error(`Adjustment failed: Product ID "${id}" not found.`);
                }

                if (product.stockQuantity + changeAmount < 0) {
                    throw new Error(`Stock adjustment would result in a negative quantity for product "${product.name}". (Current stock: ${product.stockQuantity}, attempted adjust: ${changeAmount})`);
                }

                // Apply update
                updateStmt.run(changeAmount, Date.now(), id);
            }
        });

        try {
            adjustTransaction();
            console.log('[ProductService] Bulk stock adjustments committed successfully.');
            return true;
        } catch (error) {
            console.error('[ProductService] Bulk stock adjustment transaction aborted:', error);
            throw error;
        }
    }
}

module.exports = ProductService;
