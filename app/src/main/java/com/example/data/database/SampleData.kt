package com.example.data.database

import com.example.data.entity.CustomerEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.SupplierEntity

object SampleData {
    val initialProducts = listOf(
        ProductEntity(
            name = "Aashirvaad Shudh Chakki Atta 5kg",
            hindiName = "आशीर्वाद शुद्ध चक्की आटा 5 किग्रा",
            barcode = "890103001001",
            category = "Flours & Grains / अनाज व आटा",
            purchasePrice = 210.0,
            sellingPrice = 245.0,
            unit = "kg",
            stockQuantity = 40.0,
            minStockAlert = 8.0,
            taxRate = 0.0
        ),
        ProductEntity(
            name = "Fortune Sunlite Refined Oil 1L",
            hindiName = "फॉर्च्यून रिफाइंड तेल 1 लीटर",
            barcode = "890600728101",
            category = "Oils & Ghee / तेल व घी",
            purchasePrice = 128.0,
            sellingPrice = 152.0,
            unit = "litre",
            stockQuantity = 25.0,
            minStockAlert = 5.0,
            taxRate = 5.0
        ),
        ProductEntity(
            name = "Tata Salt Vacuum Evaporated 1kg",
            hindiName = "टाटा नमक 1 किग्रा",
            barcode = "890105885233",
            category = "Spices & Salt / मसाले व नमक",
            purchasePrice = 22.0,
            sellingPrice = 28.0,
            unit = "packet",
            stockQuantity = 50.0,
            minStockAlert = 10.0,
            taxRate = 0.0
        ),
        ProductEntity(
            name = "Madhur Pure & Hygienic Sugar 1kg",
            hindiName = "मधुर शुद्ध चीनी 1 किग्रा",
            barcode = "890601441002",
            category = "Flours & Grains / अनाज व आटा",
            purchasePrice = 38.0,
            sellingPrice = 45.0,
            unit = "kg",
            stockQuantity = 80.0,
            minStockAlert = 15.0,
            taxRate = 0.0
        ),
        ProductEntity(
            name = "Tata Sampann Toor Dal Unpolished 1kg",
            hindiName = "टाटा सम्पन्न अरहर दाल 1 किग्रा",
            barcode = "890105886311",
            category = "Pulses & Dals / दालें",
            purchasePrice = 135.0,
            sellingPrice = 160.0,
            unit = "kg",
            stockQuantity = 30.0,
            minStockAlert = 6.0,
            taxRate = 0.0
        ),
        ProductEntity(
            name = "India Gate Basmati Rice Feast Rozzana 1kg",
            hindiName = "इण्डिया गेट बासमती चावल 1 किग्रा",
            barcode = "890131600122",
            category = "Flours & Grains / अनाज व आटा",
            purchasePrice = 95.0,
            sellingPrice = 120.0,
            unit = "kg",
            stockQuantity = 35.0,
            minStockAlert = 8.0,
            taxRate = 0.0
        ),
        ProductEntity(
            name = "Maggi 2-Minute Masala Noodles 70g",
            hindiName = "मैगी 2-मिनट मसाला नूडल्स 70 ग्रा",
            barcode = "890105884102",
            category = "Snacks & Instant / नमकीन व नाश्ता",
            purchasePrice = 11.5,
            sellingPrice = 14.0,
            unit = "packet",
            stockQuantity = 60.0,
            minStockAlert = 12.0,
            taxRate = 12.0
        ),
        ProductEntity(
            name = "Parle-G Gold Glucose Biscuits 100g",
            hindiName = "पारले-जी बिस्कुट 100 ग्रा",
            barcode = "890171910101",
            category = "Snacks & Instant / नमकीन व नाश्ता",
            purchasePrice = 8.2,
            sellingPrice = 10.0,
            unit = "packet",
            stockQuantity = 100.0,
            minStockAlert = 20.0,
            taxRate = 5.0
        ),
        ProductEntity(
            name = "Amul Taaza Homogenised Toned Milk 500ml",
            hindiName = "अमूल ताज़ा दूध 500 मिली",
            barcode = "890126201004",
            category = "Dairy Products / डेयरी",
            purchasePrice = 25.0,
            sellingPrice = 28.0,
            unit = "packet",
            stockQuantity = 4.0, // Low stock on purpose to show alert!
            minStockAlert = 10.0,
            taxRate = 0.0
        ),
        ProductEntity(
            name = "MDH Deggi Mirch Powder 100g",
            hindiName = "एमडीएच देगी मिर्च पाउडर 100 ग्रा",
            barcode = "890123401011",
            category = "Spices & Salt / मसाले व नमक",
            purchasePrice = 75.0,
            sellingPrice = 90.0,
            unit = "packet",
            stockQuantity = 18.0,
            minStockAlert = 5.0,
            taxRate = 5.0
        ),
        ProductEntity(
            name = "Brooke Bond Red Label Tea 250g",
            hindiName = "रेड लेबल चाय पत्ती 250 ग्रा",
            barcode = "890103038101",
            category = "Tea & Coffee / चाय व कॉफी",
            purchasePrice = 112.0,
            sellingPrice = 135.0,
            unit = "packet",
            stockQuantity = 3.0, // Low stock on purpose
            minStockAlert = 6.0,
            taxRate = 5.0
        ),
        ProductEntity(
            name = "Surf Excel Quick Wash Detergent 500g",
            hindiName = "सर्फ एक्सेल डिटर्जेंट पाउडर 500 ग्रा",
            barcode = "890103055201",
            category = "Cleaning & Household / सफाई सामग्री",
            purchasePrice = 68.0,
            sellingPrice = 82.0,
            unit = "packet",
            stockQuantity = 22.0,
            minStockAlert = 5.0,
            taxRate = 18.0
        ),
        ProductEntity(
            name = "Dettol Original Bathing Soap 75g",
            hindiName = "डेटॉल साबुन 75 ग्रा",
            barcode = "890139611202",
            category = "Personal Care / साबुन व शैम्पू",
            purchasePrice = 31.0,
            sellingPrice = 38.0,
            unit = "piece",
            stockQuantity = 2.0, // Low stock on purpose
            minStockAlert = 8.0,
            taxRate = 18.0
        ),
        ProductEntity(
            name = "Colgate Strong Teeth Toothpaste 100g",
            hindiName = "कोलगेट मंजन 100 ग्रा",
            barcode = "890131401018",
            category = "Personal Care / साबुन व शैम्पू",
            purchasePrice = 52.0,
            sellingPrice = 64.0,
            unit = "piece",
            stockQuantity = 20.0,
            minStockAlert = 5.0,
            taxRate = 18.0
        ),
        ProductEntity(
            name = "Haldiram Nagpur Aloo Bhujia 200g",
            hindiName = "हल्दीराम आलू भुजिया 200 ग्रा",
            barcode = "890400440012",
            category = "Snacks & Instant / नमकीन व नाश्ता",
            purchasePrice = 46.0,
            sellingPrice = 58.0,
            unit = "packet",
            stockQuantity = 25.0,
            minStockAlert = 6.0,
            taxRate = 12.0
        )
    )

    val initialCustomers = listOf(
        CustomerEntity(
            name = "Sharma Ji (शर्मा जी)",
            phone = "9876543210",
            address = "मकान नं. 12, गली नं. 3",
            creditLimit = 5000.0,
            totalUdhaar = 1450.0
        ),
        CustomerEntity(
            name = "Ramesh Verma (रमेश वर्मा)",
            phone = "9812345678",
            address = "दुकान के सामने, मेन रोड",
            creditLimit = 8000.0,
            totalUdhaar = 2800.0
        ),
        CustomerEntity(
            name = "Suresh Gupta (सुरेश गुप्ता - हलवाई)",
            phone = "9898765432",
            address = "गुप्ता स्वीट्स कॉर्नर",
            creditLimit = 15000.0,
            totalUdhaar = 4250.0
        ),
        CustomerEntity(
            name = "Sunita Devi (सुनीता देवी)",
            phone = "9765432109",
            address = "फ्लैट 402, शांति कुंज",
            creditLimit = 3000.0,
            totalUdhaar = 620.0
        )
    )

    val initialSuppliers = listOf(
        SupplierEntity(
            name = "Aggarwal Wholesale Traders",
            phone = "9988776655",
            company = "अग्रवाल होलसेल किराना मंडी",
            address = "शॉप 45, नई अनाज मंडी"
        ),
        SupplierEntity(
            name = "Hindustan Unilever Distributor",
            phone = "9877112233",
            company = "एचयूएल ऑथराइज़्ड एजेंसी",
            address = "इंडस्ट्रियल एरिया, फेस 2"
        ),
        SupplierEntity(
            name = "Amul Dairy Agency",
            phone = "9811223344",
            company = "अमूल मिल्क डिपो",
            address = "बस स्टैंड रोड"
        )
    )
}
