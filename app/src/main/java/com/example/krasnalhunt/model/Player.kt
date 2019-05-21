package com.example.krasnalhunt.model

import android.location.Location

 class Player {
     var location : Location
     constructor(location: Location){
         this.location = location
     }

     fun getPlayerLocation() : Location{
         return location
     }
     fun setPlayerLocation(location: Location){
         this.location=location
     }

}