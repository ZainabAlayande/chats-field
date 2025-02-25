package chats.cash.chats_field.offline

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import chats.cash.chats_field.utils.ChatsFieldConstants.VENDOR_TYPE

@Entity(tableName = "beneficiary", primaryKeys = ["firstName","lastName"])
data class Beneficiary(
    var id : Int = 0,
    var firstName : String = "",
    var lastName :String = "",
    var email : String = "",
    var phone : String = "",
    var address: String = "",
    var state: String = "",
    var country: String = "",
    var longitude : Double = 0.0,
    var latitude : Double = 0.0,
    var password : String = "",
    var status : Int = 0,
    var nfc : String = "",
    var bvn : String = "",
    var rightThumb : String = "",
    var rightLittle : String = "",
    var rightIndex : String = "",
    var leftThumb : String = "",
    var leftLittle : String= "",
    var leftIndex : String = "",
    var profilePic : String = "",
    var storeName : String = "",
    var gender : String = "",
    var date : String = "",
    var campaignId :String = "",
    var pin : String = "",
    var nin : String = "",
    var isSpecialCase : Boolean = false,
    var type : Int = VENDOR_TYPE,
    @Ignore
    var allFingers:ArrayList<Bitmap>?=null
)
