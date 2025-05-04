package br.com.slyco.slycocafe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.text.TextPaint
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter


class Receipt {
    private var maxChars: Int // Define maxChars as an object variable
    private lateinit var context: Context
    var spacing:Int
    var padding:Int
    private lateinit var dashed: String

    constructor(context: Context, printerColumns: Int = 27,padding: Int = 10,spacing: Int = 12) {
        this.context = context
        this.maxChars = printerColumns
        this.padding = padding
        this.spacing = spacing
        this.dashed = "-".repeat(maxChars)
        // Additional logic here
    }

    // Helper function to center text
    private fun center(line: String): String {
        val spaces = (maxChars - line.length).coerceAtLeast(0) / 2
        return " ".repeat(spaces) + line
    }

    // Helper function to right-align text
    private fun right(line: String): String {
        val spaces = (maxChars - line.length).coerceAtLeast(0)
        return " ".repeat(spaces) + line
    }

    private fun createBitmapFromLines(lines: List<String>, textPaint: TextPaint): Bitmap {
        val lineHeight = (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top).toInt()

        val totalHeight = (lines.size * (lineHeight + spacing))   // Ensure room for dashed line

        val charWidth = textPaint.measureText("W")
        val imageWidth = (charWidth * maxChars).toInt() + padding * 2

        val bitmap = Bitmap.createBitmap(imageWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        var y = -textPaint.fontMetrics.top
        for (rawLine in lines) {

            canvas.drawText(rawLine, padding.toFloat(), y.toFloat(), textPaint)
            y += lineHeight + spacing
        }

        return bitmap
    }

    // Method to generate a receipt header (merchant info)
    fun generateMerchantReceiptHeaderBitmap(
        merchantName: String,
        cnpj: String,
        locationName: String,
        date: String,
        time: String,
        locationId: String // Assuming formatted like AAAA-AAAA-AAAA-AAAA
    ): Bitmap {
        val textPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }

        // Format lines for the header
        val lines = listOf(
            center(merchantName.take(maxChars)),
            center("CNPJ $cnpj".take(maxChars)),
            "", // Blank line
            center(locationName.take(maxChars)),
            center("($locationId)"),
            "", // Blank line
            right("Data: $date"),
            right("Hora:   $time")
        )

        // Create and return the bitmap from the formatted lines
        return createBitmapFromLines(lines, textPaint)
    }

    // Method to generate a receipt body (items, subtotal, discount, total)
    fun generateReceiptBodyBitmap(inventoryList: MutableList<ITEM>, subtotal: Int = 0, discount: Int = 0, total: Int): Bitmap {
        val textPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }

        val headerText = "QT ITEM       (PRECO) TOTAL"

        // Create the list of lines for the body
        val lines = mutableListOf<String>()
        lines.add(dashed)
        lines.add(headerText)
        lines.add(dashed)

        // Add each inventory item
        inventoryList.forEach { item ->
            if (item.qty == 0) return@forEach

            // Format fields
            val qty = item.qty.toString().padStart(2)

            val price = (item.price ?: 0f)
            val total = price * item.qty

            val priceStr = String.format("%.2f", price).padStart(5) // 5 for price
            val totalStr = String.format("%.2f", total).padStart(5)

            // Ensure price fits in (xx.xx)
            val priceFormatted = "(${priceStr})"

            // Abbreviate item name if it has multiple words
            val nameParts = item.name.trim().split(" ")
            val displayName = if (nameParts.size >= 2) {
                "${nameParts[0].first()}.${nameParts[1]}"
            } else {
                item.name
            }

            // Limit to 10 characters, pad if short
            val itemName = displayName.take(10).padEnd(10)

            // Compose line matching header pattern
            val itemLine = "$qty $itemName $priceFormatted $totalStr"

            lines.add(itemLine)
        }



        // Add the final separator
        lines.add(dashed)

        // Add subtotal, discount, and total if necessary
        if (discount > 0) {
            lines.add(rightLabel("SUB TOTAL", subtotal))
            lines.add(rightLabel("DESCONTO", discount))
        }

        lines.add(rightLabel("TOTAL", total))

        // Create and return the bitmap from the body lines
        return createBitmapFromLines(lines, textPaint)
    }

    private fun rightLabel(label: String, valueInCents: Int, totalWidth: Int = maxChars, valueWidth: Int = 6): String {
        // Convert cents to "X,YY" string
        val valueFormatted = String.format("%,d", valueInCents / 100) + "," + String.format("%02d", valueInCents % 100)

        // Right-align the value within the fixed width (default 6 characters)
        val paddedValue = valueFormatted.padStart(valueWidth)

        // Create full label + value string
        val labelText = "$label $paddedValue"

        // Right-align the entire line
        return " ".repeat((totalWidth - labelText.length).coerceAtLeast(0)) + labelText
    }

    // Method to generate a footer image with a custom message
    fun generateFooterBitmap(message: String): Bitmap {
        val textPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }



        return createBitmapFromLines(preRenderingMuitlilineText("${dashed}\n${message}\n${dashed}"), textPaint)
    }

    fun generateCustomerReceiptBitmap(textBlock: String): Bitmap {
        val textPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }

        val lines = listOf("") + textBlock.split("\n").map { line ->
            line.take(maxChars)  // Truncate each line to maxChars
        }

        return createBitmapFromLines(lines, textPaint)
    }

    fun generateFullReceiptBitmap(
        context: Context,
        logoResId: Int,
        headerBitmap: Bitmap,
        bodyBitmap: Bitmap,
        footerBitmap: Bitmap,
        customerReceiptBitmap: Bitmap? = null,
        qrCodeBitmap: Bitmap? = null,
        postQrCodeBitmap: Bitmap? = null,
        maxChars: Int = 27,
        textSize: Float = 24f,
        extraBottomPadding: Int = 16
    ): Bitmap {
        // 1. Calculate receipt width from monospaced font
        val textPaint = TextPaint().apply {
            this.textSize = textSize
            typeface = Typeface.MONOSPACE
        }
        val charWidth = textPaint.measureText("W")
        val receiptWidth = (charWidth * maxChars).toInt()  // padding

        // 2. Load and scale logo to 1/3 width
        val logoBitmap = BitmapFactory.decodeResource(context.resources, logoResId)
        val logoWidth = receiptWidth / 3
        val logoScaled = Bitmap.createScaledBitmap(
            logoBitmap,
            logoWidth,
            (logoBitmap.height * (logoWidth.toFloat() / logoBitmap.width)).toInt(),
            true
        )

        // 3. Prepare list of all bitmaps in order
        val bitmapList = mutableListOf<Bitmap>()
        bitmapList.add(logoScaled)
        bitmapList.add(headerBitmap)
        bitmapList.add(bodyBitmap)
        bitmapList.add(footerBitmap)

        customerReceiptBitmap?.let { bitmapList.add(it) }
        qrCodeBitmap?.let { qrBitmap ->
            val qrWidth = (receiptWidth * 4) / 5
            val scaledQr = Bitmap.createScaledBitmap(
                qrBitmap,
                qrWidth,
                (qrBitmap.height * (qrWidth.toFloat() / qrBitmap.width)).toInt(),
                true
            )
            bitmapList.add(scaledQr)
        }
        postQrCodeBitmap?.let { bitmapList.add(it) }

        // 4. Compute total height
        val totalHeight = bitmapList.sumOf { it.height } + extraBottomPadding

        // 5. Create final composite bitmap
        val finalBitmap = Bitmap.createBitmap(receiptWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(finalBitmap)
        canvas.drawColor(Color.WHITE)

        // 6. Draw bitmaps vertically
        var currentY = 0
        bitmapList.forEach { bmp ->
            val left = (receiptWidth - bmp.width) / 2 // center horizontally
            canvas.drawBitmap(bmp, left.toFloat(), currentY.toFloat(), null)
            currentY += bmp.height
        }

        return finalBitmap
    }

    fun generateQrCodeBitmap(data: String, size: Int = 300): Bitmap? {
        try {
            if (data.length > 120) {
                throw IllegalArgumentException("QR code string too long (${data.length} chars). Max recommended is 120.")
            }

            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
            )

            val bitMatrix = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, size, size, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                for (x in 0 until width) {
                    pixels[y * width + x] = if (bitMatrix.get(x, y)) {
                        0xFF000000.toInt()  // black
                    } else {
                        0xFFFFFFFF.toInt()  // white
                    }
                }
            }

            return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun preRenderingMuitlilineText (message: String): List<String>{
        val lines = mutableListOf<String>()
        val maxChars = this.maxChars  // Assumes it's a class property

        // Split first on \n to handle manual line breaks
        val paragraphs = message.split("\n")

        for (paragraph in paragraphs) {
            val words = paragraph.split(" ")
            var currentLine = ""

            for (word in words) {
                if ((currentLine + " " + word).trim().length > maxChars) {
                    lines.add(currentLine.trim())
                    currentLine = word
                } else {
                    currentLine += " $word"
                }
            }

            if (currentLine.isNotBlank()) {
                lines.add(currentLine.trim())
            }
        }

        // Center and render
        return lines.map { center(it) }
    }
    fun generatePostQrCodeBitmap(message: String, fontSize: Float = 18f): Bitmap {

        val textPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = fontSize
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }


        return createBitmapFromLines(preRenderingMuitlilineText(message), textPaint)
    }

}
