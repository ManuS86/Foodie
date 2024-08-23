package com.example.foodie.services

//class MyNavigationService : Service() {
//
//    private val fusedLocationClient =
//        LocationServices.getFusedLocationProviderClient(this)
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int):
//            Int {
//        createNotificationChannel()
//        getLocation()
//        startForeground(1, notification)
//        return START_STICKY
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        TODO("Not yet implemented")
//    }
//
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val serviceChannel = NotificationChannel(
//                CHANNEL_ID,
//                "Location Service Channel",
//                NotificationManager.IMPORTANCE_HIGH
//            )
//            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            manager.createNotificationChannel(serviceChannel)
//        }
//    }
//
//    private fun getLocation() {
//        val locationRequest = LocationRequest.Builder()
//            .setPriority(LocationRequest.Priority.PRIORITY_HIGH_ACCURACY)
//            .setInterval(10000) // Update interval in milliseconds
//            .setFastestInterval(5000) // Fastest update interval in milliseconds
//
//            .build()
//
//        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback)
//    }
//        if (ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            mFusedLocationClient.requestLocationUpdates(
//                mLocationRequestHighAccuracy,
//                object : LocationCallback() {
//                    override fun onLocationResult(locationResult: LocationResult) {
//                        val location = locationResult.lastLocation
//                        if (location != null) {
//                            val latitude = location.latitude
//                            val longitude = location.longitude
//                            // Process latitude and longitude as needed
//                        }
//                    }
//                },
//                Looper.myLooper()!!
//            )
//        }
//    }
//}