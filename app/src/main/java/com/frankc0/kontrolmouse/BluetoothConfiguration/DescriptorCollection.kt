package com.frankc0.kontrolmouse.BluetoothConfiguration

object DescriptorCollection {

    // Descriptor for Absolute Mouse (Digital Pen)
    val aMouse = byteArrayOf(
        0x05.toByte(), 0x0d.toByte(),                //     USAGE_PAGE (Digitizers)
        0x09.toByte(), 0x04.toByte(),                //     USAGE (Touch Screen)
        0xa1.toByte(), 0x01.toByte(),                //     COLLECTION (Application)
        0x85.toByte(), 0x0a.toByte(),                //     REPORT_ID (0a)
        0x09.toByte(), 0x22.toByte(),                //     USAGE (Finger)
        0xa1.toByte(), 0x02.toByte(),                //     COLLECTION (Logical)
        0x09.toByte(), 0x42.toByte(),                //     USAGE (Tip Switch)
        0x15.toByte(), 0x00.toByte(),                //     LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x01.toByte(),                //     LOGICAL_MAXIMUM (1)
        0x75.toByte(), 0x01.toByte(),                //     REPORT_SIZE (1)
        0x95.toByte(), 0x01.toByte(),                //     REPORT_COUNT (1)
        0x81.toByte(), 0x02.toByte(),                //     INPUT (Data,Var,Abs)
        0x09.toByte(), 0x32.toByte(),                //     USAGE (In Range)
        0x81.toByte(), 0x02.toByte(),                //     INPUT (Data,Var,Abs)
        0x95.toByte(), 0x06.toByte(),                //     REPORT_COUNT (6)
        0x81.toByte(), 0x03.toByte(),                //     INPUT (Cnst,Ary,Abs)
        0x05.toByte(), 0x01.toByte(),                //     USAGE_PAGE (Generic Desk..

        // La pantalla se dividira 4095 x 4095
        // superior izquierda = (0, 0)
        // inferior derecha = (4095, 4095)
        0x26.toByte(), 0xff.toByte(), 0x0f.toByte(), //     LOGICAL_MAXIMUM (4095)
//        0x26.toByte(), 0x80.toByte(), 0x07.toByte(), //     LOGICAL_MAXIMUM (1920)

        0x75.toByte(), 0x10.toByte(),                //     REPORT_SIZE (16)
        0x95.toByte(), 0x01.toByte(),                //     REPORT_COUNT (1)
        0x55.toByte(), 0x0e.toByte(),                //     UNIT_EXPONENT (-2)
        0x65.toByte(), 0x13.toByte(),                //     UNIT (Inch,EngLinear)
        0x09.toByte(), 0x30.toByte(),                //     USAGE (X)
        0x35.toByte(), 0x00.toByte(),                //     PHYSICAL_MINIMUM (0)
        0x46.toByte(), 0x38.toByte(), 0x04.toByte(), //     PHYSICAL_MAXIMUM (1080)
        0x81.toByte(), 0x02.toByte(),                //     INPUT (Data,Var,Abs)
        0x46.toByte(), 0x80.toByte(), 0x07.toByte(), //     PHYSICAL_MAXIMUM (1920)
        0x09.toByte(), 0x31.toByte(),                //     USAGE (Y)
        0x81.toByte(), 0x02.toByte(),                //     INPUT (Data,Var,Abs)
        0xc0.toByte(),                               //     END_COLLECTION
        0xc0.toByte(),                               //     END_COLLECTION
    )
}