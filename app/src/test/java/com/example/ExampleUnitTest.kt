package com.example

import com.example.data.Sale
import com.example.data.SaleItem
import com.example.ui.components.ReceiptPrinter
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testReceiptHtmlGeneration() {
    val sale = Sale(
        id = "test-sale-id-12345678",
        timestamp = 1689595200000L,
        customerId = "CUST123",
        customerName = "Test Customer",
        subtotal = 1000.0,
        discount = 50.0,
        tax = 19.0,
        totalAmount = 969.0,
        paymentMethod = "Cash",
        cashierName = "John Doe"
    )

    val items = listOf(
        SaleItem(
            saleId = "test-sale-id-12345678",
            productId = "PROD1",
            productName = "Premium Basmati Rice",
            quantity = 2,
            purchasePrice = 300.0,
            salePrice = 500.0,
            totalLinePrice = 1000.0
        )
    )

    val html = ReceiptPrinter.generateReceiptHtml(
        shopName = "Zam Zam Whole Sale",
        shopAddress = "Main Bazaar Ghalanai",
        shopPhone = "0300-1234567",
        shopCurrency = "PKR",
        taxRate = 2.0,
        sale = sale,
        items = items
    )

    assertNotNull(html)
    assertTrue(html.contains("Zam Zam Whole Sale"))
    assertTrue(html.contains("Main Bazaar Ghalanai"))
    assertTrue(html.contains("0300-1234567"))
    assertTrue(html.contains("Premium Basmati Rice"))
    assertTrue(html.contains("John Doe"))
    assertTrue(html.contains("Test Customer"))
    assertTrue(html.contains("1,000.00")) // formatted subtotal
    assertTrue(html.contains("50.00")) // formatted discount
    assertTrue(html.contains("969.00")) // formatted total paid
    assertTrue(html.contains("PKR"))
  }
}
