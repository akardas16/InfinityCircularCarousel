package com.akardas16.infinitycircularcarousel

data class LocationItem(
    val image:Int,
    val title:String,
    val subtitle:String,
    val rating:Int,
)

val locations = listOf(
    LocationItem(
        image = R.drawable.tabriz,
        title = "Tabriz",
        subtitle = "A city in iran",
        rating = 5
    ),
    LocationItem(
        image = R.drawable.dubai,
        subtitle = "A city in United Arab Emirates",
        rating = 3,
        title = "Dubai"
    ),
    LocationItem(
        image = R.drawable.los_angeles,
        title = "Los Angeles",
        rating = 4,
        subtitle = "A sprawling Southern California city",
    ),
    LocationItem(
        image = R.drawable.london,
        title = "London",
        rating = 3,
        subtitle = "Capital of England and the United Kingdom",
    ),
    LocationItem(
        image = R.drawable.sweden,
        title = "Sweden",
        rating = 5,
        subtitle = "A beautiful country",
    ),
    LocationItem(
        image = R.drawable.kazan,
        title = "Kazan",
        rating = 2,
        subtitle = "A city in russia",
    ),
)
