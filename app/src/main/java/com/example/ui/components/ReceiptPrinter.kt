package com.example.ui.components

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.Sale
import com.example.data.SaleItem
import com.example.ui.POSViewModel
import java.text.SimpleDateFormat
import java.util.*

object ReceiptPrinter {

    /**
     * Generates a beautifully formatted, printer-friendly HTML string for a receipt.
     */
    fun generateReceiptHtml(
        shopName: String,
        shopAddress: String,
        shopPhone: String,
        shopCurrency: String,
        taxRate: Double,
        sale: Sale,
        items: List<SaleItem>
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(sale.timestamp))
        val invoiceId = sale.id.take(8).uppercase(Locale.getDefault())

        val itemRows = StringBuilder()
        for (item in items) {
            itemRows.append(
                """
                <tr>
                    <td>${item.productName}</td>
                    <td class="text-center">${item.quantity}</td>
                    <td class="text-right">${String.format(Locale.US, "%,.2f", item.salePrice)}</td>
                    <td class="text-right">${String.format(Locale.US, "%,.2f", item.totalLinePrice)}</td>
                </tr>
                """.trimIndent()
            )
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    font-family: 'Courier New', Courier, monospace;
                    margin: 0;
                    padding: 10px;
                    color: #000;
                    font-size: 11px;
                    line-height: 1.3;
                    background-color: #fff;
                }
                .receipt {
                    max-width: 80mm;
                    margin: 0 auto;
                    padding: 5px;
                }
                .header {
                    text-align: center;
                    margin-bottom: 12px;
                }
                .shop-name {
                    font-size: 15px;
                    font-weight: bold;
                    text-transform: uppercase;
                    margin: 0 0 4px 0;
                }
                .shop-details {
                    font-size: 9px;
                    margin: 2px 0;
                    color: #333;
                }
                .divider {
                    border-top: 1px dashed #000;
                    margin: 8px 0;
                }
                .details-table {
                    width: 100%;
                    font-size: 9px;
                    margin-bottom: 8px;
                }
                .details-table td {
                    padding: 1px 0;
                }
                .items-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-bottom: 8px;
                }
                .items-table th {
                    border-bottom: 1px solid #000;
                    text-align: left;
                    padding: 3px 0;
                    font-size: 9px;
                    font-weight: bold;
                }
                .items-table td {
                    padding: 3px 0;
                    font-size: 9px;
                    vertical-align: top;
                    word-break: break-word;
                }
                .text-right {
                    text-align: right;
                }
                .text-center {
                    text-align: center;
                }
                .totals-table {
                    width: 100%;
                    margin-top: 4px;
                    font-size: 9px;
                }
                .totals-table td {
                    padding: 2px 0;
                }
                .grand-total {
                    font-size: 11px;
                    font-weight: bold;
                    border-top: 1px dashed #000;
                    border-bottom: 1px dashed #000;
                    padding: 4px 0 !important;
                }
                .footer {
                    text-align: center;
                    margin-top: 15px;
                    font-size: 8px;
                    color: #555;
                }
                .footer-thankyou {
                    font-weight: bold;
                    font-size: 10px;
                    margin-bottom: 4px;
                    color: #000;
                }
                @media print {
                    body {
                        padding: 0;
                        margin: 0;
                    }
                    .receipt {
                        max-width: 100%;
                        width: 100%;
                    }
                }
            </style>
        </head>
        <body>
            <div class="receipt">
                <div class="header">
                    <div class="shop-name">$shopName</div>
                    <div class="shop-details">$shopAddress</div>
                    <div class="shop-details">Phone: $shopPhone</div>
                </div>
                
                <div class="divider"></div>
                
                <table class="details-table">
                    <tr>
                        <td><strong>Invoice:</strong> #$invoiceId</td>
                        <td class="text-right"><strong>Date:</strong> $formattedDate</td>
                    </tr>
                    <tr>
                        <td><strong>Cashier:</strong> ${sale.cashierName}</td>
                        <td class="text-right"><strong>Customer:</strong> ${sale.customerName}</td>
                    </tr>
                    <tr>
                        <td><strong>Payment:</strong> ${sale.paymentMethod}</td>
                        <td class="text-right"><strong>Status:</strong> ${sale.status}</td>
                    </tr>
                </table>
                
                <div class="divider"></div>
                
                <table class="items-table">
                    <thead>
                        <tr>
                            <th style="width: 45%;">ITEM</th>
                            <th class="text-center" style="width: 15%;">QTY</th>
                            <th class="text-right" style="width: 20%;">PRICE</th>
                            <th class="text-right" style="width: 20%;">TOTAL</th>
                        </tr>
                    </thead>
                    <tbody>
                        $itemRows
                    </tbody>
                </table>
                
                <div class="divider"></div>
                
                <table class="totals-table">
                    <tr>
                        <td>Subtotal:</td>
                        <td class="text-right">${String.format(Locale.US, "%,.2f", sale.subtotal)} $shopCurrency</td>
                    </tr>
                    <tr>
                        <td>Discount:</td>
                        <td class="text-right">-${String.format(Locale.US, "%,.2f", sale.discount)} $shopCurrency</td>
                    </tr>
                    <tr>
                        <td>Tax (${String.format(Locale.US, "%.1f", taxRate)}%):</td>
                        <td class="text-right">+${String.format(Locale.US, "%,.2f", sale.tax)} $shopCurrency</td>
                    </tr>
                    <tr class="grand-total">
                        <td><strong>TOTAL PAID:</strong></td>
                        <td class="text-right"><strong>${String.format(Locale.US, "%,.2f", sale.totalAmount)} $shopCurrency</strong></td>
                    </tr>
                </table>
                
                <div class="divider"></div>
                
                <div class="footer">
                    <div class="footer-thankyou">THANK YOU FOR YOUR PATRONAGE!</div>
                    <div>No Return or Exchange without Receipt.</div>
                    <div style="margin-top: 4px; font-size: 7px; color: #777;">Powered by AI Studio POS</div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Integrates with Android PrintManager to print or export the receipt as PDF.
     */
    fun printReceipt(
        context: Context,
        sale: Sale,
        items: List<SaleItem>,
        viewModel: POSViewModel
    ) {
        val htmlContent = generateReceiptHtml(
            shopName = viewModel.shopName,
            shopAddress = viewModel.shopAddress,
            shopPhone = viewModel.shopPhone,
            shopCurrency = viewModel.shopCurrency,
            taxRate = viewModel.posTaxRate,
            sale = sale,
            items = items
        )

        val jobName = "${viewModel.shopName.replace(" ", "_")}_Receipt_${sale.id.take(8)}"

        // Since WebView needs to be created on the Main (UI) thread, we execute this safely.
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                if (printManager != null) {
                    val printAdapter = webView.createPrintDocumentAdapter(jobName)
                    val attributes = PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.JPN_YOU4) // Compact media size approximation
                        .build()
                    printManager.print(jobName, printAdapter, attributes)
                }
            }
        }
        // Load the formatted HTML string into WebView
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }
}
