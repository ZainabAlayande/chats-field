package chats.cash.chats_field.utils.nfc


import android.nfc.NdefMessage
import android.nfc.NdefRecord


/**
 * Created by Qifan on 05/12/2018.
 */

object NdefMessageParser {

    fun parse(message: NdefMessage): List<ParsedNdefRecord> {
        return getRecords(message.records)
    }

    fun getRecords(records: Array<NdefRecord>): List<ParsedNdefRecord> {
        val elements = ArrayList<ParsedNdefRecord>()

        for (record in records) {
            if (TextRecord.isText(record)) {
                TextRecord.parse(record)?.let { elements.add(it) }
            } else {
                elements.add(object : ParsedNdefRecord {
                    override fun str(): String {
                        return String(record.payload)
                    }
                })
            }
        }

        return elements
    }
}


/**
 * Created by Qifan on 05/12/2018.
 */

interface ParsedNdefRecord {
    fun str(): String
}