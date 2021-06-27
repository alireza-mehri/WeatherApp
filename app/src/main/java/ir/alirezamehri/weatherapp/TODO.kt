package ir.alirezamehri.weatherapp

data class TODO (
    var userID:Int,
    var id:Int,
    var title:String,
    var completed:Boolean
){
    override fun toString(): String {
        return "TODO(userID=$userID, id=$id, title='$title', completed=$completed)"
    }
}