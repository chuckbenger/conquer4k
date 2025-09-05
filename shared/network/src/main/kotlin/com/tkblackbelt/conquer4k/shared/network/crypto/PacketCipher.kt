package com.tkblackbelt.conquer4k.shared.network.crypto

import kotlinx.io.Buffer

/**
 * Packet Cipher Implemented in the 4298 client version.
 * The is essentially a simple XOR cipher with a few extra steps.
 * Each byte is XORed with 0xAB, then the nibbles are swapped, and finally the byte is XORed with two keys.
 * The client and server encrypt and decrypt with the logic reversed, so the the XOR cipher can be undone at both ends.
 *
 * Each key is 256 bytes and two 4 byte counters are used to track the counters for outgoing and incoming packets and
 * used to index into the key arrays.
 *
 *  *************************************************************
 *  *                            FUNCTION
 *  *************************************************************
 *                              undefined  __thiscall  Encryption (void *  this ,  int  para
 *                                assume FS_OFFSET = 0xffdff000
 *              undefined         AL:1           <RETURN>
 *              void *            ECX:4 (auto)   this
 *              int               Stack[0x4]:4   param_1                                 XREF[1]:     004da95d (R)
 *              int               Stack[0x8]:4   param_2                                 XREF[2]:     004da94d (R) ,
 *                                                                                                    004da9a3 (R)
 *              char              Stack[0xc]:1   param_3                                 XREF[1]:     004da9ad (R)
 *              undefined4        Stack[-0x8]:4  local_8                                 XREF[2]:     004da945 (W) ,
 *                                                                                                    004da9b3 (R)
 *              undefined4        Stack[-0xc]:4  local_c                                 XREF[2]:     004da950 (W) ,
 *                                                                                                    004da9b8 (R)
 *                              Encryption                                      XREF[3]:     ProcessAndEncrypt:004da60b  (c) ,
 *                                                                                           ReceiveManyBytes:004daa2e  (c) ,
 *                                                                                           ReceiveManyBytes:004daa7a  (c)
 *         004da93e 55              PUSH       EBP
 *         004da93f 8b  ec           MOV        EBP ,ESP
 *         004da941 51              PUSH       this
 *         004da942 51              PUSH       this
 *         004da943 8b  01           MOV        EAX ,dword ptr [this ]
 *         004da945 89  45  fc       MOV        dword ptr [EBP  + local_8 ],EAX
 *         004da948 8b  41  04       MOV        EAX ,dword ptr [this  + 0x4 ]
 *         004da94b 33  d2           XOR        EDX ,EDX
 *         004da94d 39  55  0c       CMP        dword ptr [EBP  + param_2 ],EDX
 *         004da950 89  45  f8       MOV        dword ptr [EBP  + local_c ],EAX
 *         004da953 7e  58           JLE        LAB_004da9ad
 *         004da955 53              PUSH       EBX
 *         004da956 56              PUSH       ESI
 *         004da957 57              PUSH       EDI
 *         004da958 bf  00  01       MOV        EDI ,0x100
 *                  00  00
 *                              LAB_004da95d                                    XREF[1]:     004da9a8 (j)
 *         004da95d 8b  45  08       MOV        EAX ,dword ptr [EBP  + param_1 ]
 *         004da960 8d  34  02       LEA        ESI ,[EDX  + EAX *0x1 ]
 *         004da963 8b  01           MOV        EAX ,dword ptr [this ]
 *         004da965 8a  44  08       MOV        AL ,byte ptr [EAX  + this *0x1  + 0x8 ]
 *                  08
 *         004da969 30  06           XOR        byte ptr [ESI ],AL
 *         004da96b 8b  59  04       MOV        EBX ,dword ptr [this  + 0x4 ]
 *         004da96e 8a  06           MOV        AL ,byte ptr [ESI ]
 *         004da970 8a  9c  0b       MOV        BL ,byte ptr [EBX  + this *0x1  + 0x108 ]
 *                  08  01  00
 *                  00
 *         004da977 32  d8           XOR        BL ,AL
 *         004da979 88  1e           MOV        byte ptr [ESI ],BL
 *         004da97b ff  01           INC        dword ptr [this ]
 *         004da97d 39  39           CMP        dword ptr [this ],EDI
 *         004da97f 7c  0f           JL         LAB_004da990
 *         004da981 83  21  00       AND        dword ptr [this ],0x0
 *         004da984 ff  41  04       INC        dword ptr [this  + 0x4 ]
 *         004da987 39  79  04       CMP        dword ptr [this  + 0x4 ],EDI
 *         004da98a 7c  04           JL         LAB_004da990
 *         004da98c 83  61  04       AND        dword ptr [this  + 0x4 ],0x0
 *                  00
 *                              LAB_004da990                                    XREF[2]:     004da97f (j) ,  004da98a (j)
 *         004da990 8a  06           MOV        AL ,byte ptr [ESI ]
 *         004da992 8a  d8           MOV        BL ,AL
 *         004da994 80  e3  0f       AND        BL ,0xf
 *         004da997 c0  e3  04       SHL        BL ,0x4
 *         004da99a c0  e8  04       SHR        AL ,0x4
 *         004da99d 02  d8           ADD        BL ,AL
 *         004da99f 80  f3  ab       XOR        BL ,0xab
 *         004da9a2 42              INC        EDX
 *         004da9a3 3b  55  0c       CMP        EDX ,dword ptr [EBP  + param_2 ]
 *         004da9a6 88  1e           MOV        byte ptr [ESI ],BL
 *         004da9a8 7c  b3           JL         LAB_004da95d
 *         004da9aa 5f              POP        EDI
 *         004da9ab 5e              POP        ESI
 *         004da9ac 5b              POP        EBX
 *                              LAB_004da9ad                                    XREF[1]:     004da953 (j)
 *         004da9ad 80  7d  10       CMP        byte ptr [EBP  + param_3 ],0x0
 *                  00
 *         004da9b1 75  0b           JNZ        LAB_004da9be
 *         004da9b3 8b  45  fc       MOV        EAX ,dword ptr [EBP  + local_8 ]
 *         004da9b6 89  01           MOV        dword ptr [this ],EAX
 *         004da9b8 8b  45  f8       MOV        EAX ,dword ptr [EBP  + local_c ]
 *         004da9bb 89  41  04       MOV        dword ptr [this  + 0x4 ],EAX
 *                              LAB_004da9be                                    XREF[1]:     004da9b1 (j)
 *         004da9be c9              LEAVE
 *         004da9bf c2  0c  00       RET        0xc
 */
abstract class PacketCipher {
    private var inCounter: Int = 0
    private var outCounter: Int = 0

    fun encrypt(buffer: Buffer) {
        val availableBytes = buffer.size.toInt()
        for (index in 0 until availableBytes) {
            buffer.writeByte(encryptByte(buffer.readByte()))
        }
    }

    fun decrypt(buffer: Buffer) {
        val availableBytes = buffer.size.toInt()
        for (index in 0 until availableBytes) {
            buffer.writeByte(decryptByte(buffer.readByte()))
        }
    }

    fun decryptShortLe(short: Short): Short {
        val lowByte = (short.toInt() and 0xFF).toByte()
        val highByte = ((short.toInt() shr 8) and 0xFF).toByte()
        val encryptedLow = decryptByte(lowByte)
        val encryptedHigh = decryptByte(highByte)
        val encryptedShort = ((encryptedHigh.toInt() and 0xFF) shl 8) or (encryptedLow.toInt() and 0xFF)
        return encryptedShort.toShort()
    }

    fun encryptShortLe(short: Short): Short {
        val lowByte = (short.toInt() and 0xFF).toByte()
        val highByte = ((short.toInt() shr 8) and 0xFF).toByte()
        val encryptedLow = encryptByte(lowByte)
        val encryptedHigh = encryptByte(highByte)
        val encryptedShort = ((encryptedHigh.toInt() and 0xFF) shl 8) or (encryptedLow.toInt() and 0xFF)
        return encryptedShort.toShort()
    }

    private fun encryptByte(byte: Byte): Byte {
        val encryptedByte = rollOne(byte, outCounter)
        outCounter = (outCounter + 1) and 0xFFFF
        return encryptedByte
    }

    private fun decryptByte(byte: Byte): Byte {
        val decryptedByte = rollOne(byte, inCounter)
        inCounter = (inCounter + 1) and 0xFFFF
        return decryptedByte
    }

    protected abstract fun rollOne(
        byte: Byte,
        counter: Int,
    ): Byte

    companion object {
        @JvmStatic
        protected val staticKey1 =
            byteArrayOf(
                0x9D.toByte(),
                0x90.toByte(),
                0x83.toByte(),
                0x8A.toByte(),
                0xD1.toByte(),
                0x8C.toByte(),
                0xE7.toByte(),
                0xF6.toByte(),
                0x25.toByte(),
                0x28.toByte(),
                0xEB.toByte(),
                0x82.toByte(),
                0x99.toByte(),
                0x64.toByte(),
                0x8F.toByte(),
                0x2E.toByte(),
                0x2D.toByte(),
                0x40.toByte(),
                0xD3.toByte(),
                0xFA.toByte(),
                0xE1.toByte(),
                0xBC.toByte(),
                0xB7.toByte(),
                0xE6.toByte(),
                0xB5.toByte(),
                0xD8.toByte(),
                0x3B.toByte(),
                0xF2.toByte(),
                0xA9.toByte(),
                0x94.toByte(),
                0x5F.toByte(),
                0x1E.toByte(),
                0xBD.toByte(),
                0xF0.toByte(),
                0x23.toByte(),
                0x6A.toByte(),
                0xF1.toByte(),
                0xEC.toByte(),
                0x87.toByte(),
                0xD6.toByte(),
                0x45.toByte(),
                0x88.toByte(),
                0x8B.toByte(),
                0x62.toByte(),
                0xB9.toByte(),
                0xC4.toByte(),
                0xC4.toByte(),
                0x2F.toByte(),
                0x0E.toByte(),
                0x4D.toByte(),
                0xA0.toByte(),
                0x73.toByte(),
                0xDA.toByte(),
                0x01.toByte(),
                0x1C.toByte(),
                0x57.toByte(),
                0xC6.toByte(),
                0xD5.toByte(),
                0x38.toByte(),
                0xDB.toByte(),
                0xD2.toByte(),
                0xC9.toByte(),
                0xF4.toByte(),
                0xFF.toByte(),
                0xFE.toByte(),
                0xDD.toByte(),
                0x50.toByte(),
                0xC3.toByte(),
                0x4A.toByte(),
                0x11.toByte(),
                0x4C.toByte(),
                0x27.toByte(),
                0xB6.toByte(),
                0x65.toByte(),
                0xE8.toByte(),
                0x2B.toByte(),
                0x42.toByte(),
                0xD9.toByte(),
                0x24.toByte(),
                0xCF.toByte(),
                0xEE.toByte(),
                0x6D.toByte(),
                0x00.toByte(),
                0x13.toByte(),
                0xBA.toByte(),
                0x21.toByte(),
                0x7C.toByte(),
                0xF7.toByte(),
                0xA6.toByte(),
                0xF5.toByte(),
                0x98.toByte(),
                0x7B.toByte(),
                0xB2.toByte(),
                0xE9.toByte(),
                0x54.toByte(),
                0x9F.toByte(),
                0xDE.toByte(),
                0xFD.toByte(),
                0xB0.toByte(),
                0x63.toByte(),
                0x2A.toByte(),
                0x31.toByte(),
                0xAC.toByte(),
                0xC7.toByte(),
                0x96.toByte(),
                0x85.toByte(),
                0x48.toByte(),
                0xCB.toByte(),
                0x22.toByte(),
                0xF9.toByte(),
                0x84.toByte(),
                0x6F.toByte(),
                0xCE.toByte(),
                0x8D.toByte(),
                0x60.toByte(),
                0xB3.toByte(),
                0x9A.toByte(),
                0x41.toByte(),
                0xDC.toByte(),
                0x97.toByte(),
                0x86.toByte(),
                0x15.toByte(),
                0xF8.toByte(),
                0x1B.toByte(),
                0x92.toByte(),
                0x09.toByte(),
                0xB4.toByte(),
                0x3F.toByte(),
                0xBE.toByte(),
                0x1D.toByte(),
                0x10.toByte(),
                0x03.toByte(),
                0x0A.toByte(),
                0x51.toByte(),
                0x0C.toByte(),
                0x67.toByte(),
                0x76.toByte(),
                0xA5.toByte(),
                0xA8.toByte(),
                0x6B.toByte(),
                0x02.toByte(),
                0x19.toByte(),
                0xE4.toByte(),
                0x0F.toByte(),
                0xAE.toByte(),
                0xAD.toByte(),
                0xC0.toByte(),
                0x53.toByte(),
                0x7A.toByte(),
                0x61.toByte(),
                0x3C.toByte(),
                0x37.toByte(),
                0x66.toByte(),
                0x35.toByte(),
                0x58.toByte(),
                0xBB.toByte(),
                0x72.toByte(),
                0x29.toByte(),
                0x14.toByte(),
                0xDF.toByte(),
                0x9E.toByte(),
                0x3D.toByte(),
                0x70.toByte(),
                0xA3.toByte(),
                0xEA.toByte(),
                0x71.toByte(),
                0x6C.toByte(),
                0x07.toByte(),
                0x56.toByte(),
                0xC5.toByte(),
                0x08.toByte(),
                0x0B.toByte(),
                0xE2.toByte(),
                0x39.toByte(),
                0x44.toByte(),
                0xAF.toByte(),
                0x8E.toByte(),
                0xCD.toByte(),
                0x20.toByte(),
                0xF3.toByte(),
                0x5A.toByte(),
                0x81.toByte(),
                0x9C.toByte(),
                0xD7.toByte(),
                0x46.toByte(),
                0x55.toByte(),
                0xB8.toByte(),
                0x5B.toByte(),
                0x52.toByte(),
                0x49.toByte(),
                0x74.toByte(),
                0x7F.toByte(),
                0x7E.toByte(),
                0x5D.toByte(),
                0xD0.toByte(),
                0x43.toByte(),
                0xCA.toByte(),
                0x91.toByte(),
                0xCC.toByte(),
                0xA7.toByte(),
                0x36.toByte(),
                0xE5.toByte(),
                0x68.toByte(),
                0xAB.toByte(),
                0xC2.toByte(),
                0x59.toByte(),
                0xA4.toByte(),
                0x4F.toByte(),
                0x6E.toByte(),
                0xED.toByte(),
                0x80.toByte(),
                0x93.toByte(),
                0x3A.toByte(),
                0xA1.toByte(),
                0xFC.toByte(),
                0x77.toByte(),
                0x26.toByte(),
                0x75.toByte(),
                0x18.toByte(),
                0xFB.toByte(),
                0x32.toByte(),
                0x69.toByte(),
                0xD4.toByte(),
                0x1F.toByte(),
                0x5E.toByte(),
                0x7D.toByte(),
                0x30.toByte(),
                0xE3.toByte(),
                0xAA.toByte(),
                0xB1.toByte(),
                0x2C.toByte(),
                0x47.toByte(),
                0x16.toByte(),
                0x05.toByte(),
                0xC8.toByte(),
                0x4B.toByte(),
                0xA2.toByte(),
                0x79.toByte(),
                0x04.toByte(),
                0xEF.toByte(),
                0x4E.toByte(),
                0x0D.toByte(),
                0xE0.toByte(),
                0x33.toByte(),
                0x1A.toByte(),
                0xC1.toByte(),
                0x5C.toByte(),
                0x5C.toByte(),
                0x17.toByte(),
                0x06.toByte(),
                0x95.toByte(),
                0x78.toByte(),
                0x9B.toByte(),
                0x12.toByte(),
                0x89.toByte(),
                0x34.toByte(),
                0xBF.toByte(),
                0x3E.toByte(),
            )

        @JvmStatic
        protected val staticKey2 =
            byteArrayOf(
                0x62.toByte(),
                0x4F.toByte(),
                0xE8.toByte(),
                0x15.toByte(),
                0xDE.toByte(),
                0xEB.toByte(),
                0x04.toByte(),
                0x91.toByte(),
                0x1A.toByte(),
                0xC7.toByte(),
                0xE0.toByte(),
                0x4D.toByte(),
                0x16.toByte(),
                0xE3.toByte(),
                0x7C.toByte(),
                0x49.toByte(),
                0xD2.toByte(),
                0x3F.toByte(),
                0xD8.toByte(),
                0x85.toByte(),
                0x4E.toByte(),
                0xDB.toByte(),
                0xF4.toByte(),
                0x01.toByte(),
                0x8A.toByte(),
                0xB7.toByte(),
                0xD0.toByte(),
                0xBD.toByte(),
                0x86.toByte(),
                0xD3.toByte(),
                0x6C.toByte(),
                0xB9.toByte(),
                0x42.toByte(),
                0x2F.toByte(),
                0xC8.toByte(),
                0xF5.toByte(),
                0xBE.toByte(),
                0xCB.toByte(),
                0xE4.toByte(),
                0x71.toByte(),
                0xFA.toByte(),
                0xA7.toByte(),
                0xC0.toByte(),
                0x2D.toByte(),
                0xF6.toByte(),
                0xC3.toByte(),
                0x5C.toByte(),
                0x29.toByte(),
                0xB2.toByte(),
                0x1F.toByte(),
                0xB8.toByte(),
                0x65.toByte(),
                0x2E.toByte(),
                0xBB.toByte(),
                0xD4.toByte(),
                0xE1.toByte(),
                0x6A.toByte(),
                0x97.toByte(),
                0xB0.toByte(),
                0x9D.toByte(),
                0x66.toByte(),
                0xB3.toByte(),
                0x4C.toByte(),
                0x99.toByte(),
                0x22.toByte(),
                0x0F.toByte(),
                0xA8.toByte(),
                0xD5.toByte(),
                0x9E.toByte(),
                0xAB.toByte(),
                0xC4.toByte(),
                0x51.toByte(),
                0xDA.toByte(),
                0x87.toByte(),
                0xA0.toByte(),
                0x0D.toByte(),
                0xD6.toByte(),
                0xA3.toByte(),
                0x3C.toByte(),
                0x09.toByte(),
                0x92.toByte(),
                0xFF.toByte(),
                0x98.toByte(),
                0x45.toByte(),
                0x0E.toByte(),
                0x9B.toByte(),
                0xB4.toByte(),
                0xC1.toByte(),
                0x4A.toByte(),
                0x77.toByte(),
                0x90.toByte(),
                0x7D.toByte(),
                0x46.toByte(),
                0x93.toByte(),
                0x2C.toByte(),
                0x79.toByte(),
                0x02.toByte(),
                0xEF.toByte(),
                0x88.toByte(),
                0xB5.toByte(),
                0x7E.toByte(),
                0x8B.toByte(),
                0xA4.toByte(),
                0x31.toByte(),
                0xBA.toByte(),
                0x67.toByte(),
                0x80.toByte(),
                0xED.toByte(),
                0xB6.toByte(),
                0x83.toByte(),
                0x1C.toByte(),
                0xE9.toByte(),
                0x95.toByte(),
                0x5E.toByte(),
                0x6B.toByte(),
                0x84.toByte(),
                0x11.toByte(),
                0x9A.toByte(),
                0x47.toByte(),
                0x60.toByte(),
                0xCD.toByte(),
                0x96.toByte(),
                0x63.toByte(),
                0xFC.toByte(),
                0xC9.toByte(),
                0x52.toByte(),
                0xBF.toByte(),
                0x58.toByte(),
                0x05.toByte(),
                0xCE.toByte(),
                0x5B.toByte(),
                0x74.toByte(),
                0x81.toByte(),
                0x0A.toByte(),
                0x37.toByte(),
                0x50.toByte(),
                0x3D.toByte(),
                0x06.toByte(),
                0x53.toByte(),
                0xEC.toByte(),
                0x39.toByte(),
                0xC2.toByte(),
                0xAF.toByte(),
                0x48.toByte(),
                0x75.toByte(),
                0x3E.toByte(),
                0x4B.toByte(),
                0x64.toByte(),
                0xF1.toByte(),
                0x7A.toByte(),
                0x27.toByte(),
                0x40.toByte(),
                0xAD.toByte(),
                0x76.toByte(),
                0x43.toByte(),
                0xDC.toByte(),
                0xA9.toByte(),
                0x32.toByte(),
                0x9F.toByte(),
                0x38.toByte(),
                0xE5.toByte(),
                0xAE.toByte(),
                0x3B.toByte(),
                0x54.toByte(),
                0x61.toByte(),
                0xEA.toByte(),
                0x17.toByte(),
                0x30.toByte(),
                0x1D.toByte(),
                0xE6.toByte(),
                0x33.toByte(),
                0xCC.toByte(),
                0x19.toByte(),
                0xA2.toByte(),
                0x8F.toByte(),
                0x28.toByte(),
                0x55.toByte(),
                0x1E.toByte(),
                0x2B.toByte(),
                0x44.toByte(),
                0xD1.toByte(),
                0x5A.toByte(),
                0x07.toByte(),
                0x20.toByte(),
                0x8D.toByte(),
                0x56.toByte(),
                0x23.toByte(),
                0xBC.toByte(),
                0x89.toByte(),
                0x12.toByte(),
                0x7F.toByte(),
                0x18.toByte(),
                0xC5.toByte(),
                0x8E.toByte(),
                0x1B.toByte(),
                0x34.toByte(),
                0x41.toByte(),
                0xCA.toByte(),
                0xF7.toByte(),
                0x10.toByte(),
                0xFD.toByte(),
                0xC6.toByte(),
                0x13.toByte(),
                0xAC.toByte(),
                0xF9.toByte(),
                0x82.toByte(),
                0x6F.toByte(),
                0x08.toByte(),
                0x35.toByte(),
                0xFE.toByte(),
                0x0B.toByte(),
                0x24.toByte(),
                0xB1.toByte(),
                0x3A.toByte(),
                0xE7.toByte(),
                0x00.toByte(),
                0x6D.toByte(),
                0x36.toByte(),
                0x03.toByte(),
                0x9C.toByte(),
                0x69.toByte(),
                0xF2.toByte(),
                0x5F.toByte(),
                0xF8.toByte(),
                0xA5.toByte(),
                0x6E.toByte(),
                0xFB.toByte(),
                0x14.toByte(),
                0x21.toByte(),
                0xAA.toByte(),
                0xD7.toByte(),
                0xF0.toByte(),
                0xDD.toByte(),
                0xA6.toByte(),
                0xF3.toByte(),
                0x8C.toByte(),
                0xD9.toByte(),
            )
    }
}
