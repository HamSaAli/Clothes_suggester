package data.source

import com.example.clothes_suggester.R
import data.Clothing

object LocalDataSource {
    val tooHeavyClothes = listOf(
        Clothing(R.drawable.hoodie_white),
        Clothing(R.drawable.jacket_brown),
        Clothing(R.drawable.sweater_skyblue),
        Clothing(R.drawable.sweater_green),
        Clothing(R.drawable.jeans_brown),
        Clothing(R.drawable.jeans_black),
    )
    val heavyClothes = listOf(
        Clothing(R.drawable.hoodie_white),
        Clothing(R.drawable.jacket_brown),
        Clothing(R.drawable.sweater_skyblue),
        Clothing(R.drawable.sweater_green),
        Clothing(R.drawable.jeans_brown),
        Clothing(R.drawable.jeans_black),
    )
    val springClothes = listOf(
        Clothing(R.drawable.dress_black),
        Clothing(R.drawable.skirt_blue_white_jacket),
        Clothing(R.drawable.skirt_black),
        Clothing(R.drawable.skirt_blue),
        Clothing(R.drawable.jeans_brown),
        Clothing(R.drawable.jeans_black),
    )
    val lightClothes = listOf(
        Clothing(R.drawable.tshirt_white),
        Clothing(R.drawable.tshirt_green),
        Clothing(R.drawable.tshirt_pink),
        Clothing(R.drawable.tshirt_brown),
        Clothing(R.drawable.tshirt_striped_red),
        Clothing(R.drawable.jeans_brown),
        Clothing(R.drawable.jeans_black),
        Clothing(R.drawable.shirt),
        Clothing(R.drawable.jeans_black),
        Clothing(R.drawable.dress_black),
        Clothing(R.drawable.dress_orange),
        Clothing(R.drawable.skirt_blue),
        Clothing(R.drawable.skirt_black),
    )
}