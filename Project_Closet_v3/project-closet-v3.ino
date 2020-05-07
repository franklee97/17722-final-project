#include "Particle.h"

#define BEACON_UUID 0xfeaa
#define BEACON_RE_CHECK_MS 7500
#define SCAN_TIMEOUT_10MS 500

#define REEDPIN 4

boolean umbrella_found = false;
boolean rain = false;
boolean first_read = false;
// Used to store the address of the device we're looking for
BleAddress searchAddress;

int8_t closet_open = 0;
int8_t prev_closet_open = 0;

// Stores the most recent data related to the device we're looking for
int8_t lastRSSI;
system_tick_t lastSeen = 0;

// The payload going to the cloud
String status;

float feels_data_fah = 0.0;
String cloud_data;
float wind_data;

// For logging
SerialLogHandler logHandler(115200, LOG_LEVEL_ERROR, {
    { "app", LOG_LEVEL_TRACE }, // enable all app messages
});

void setup() {
    //(void)logHandler; // Does nothing, just to eliminate warning for unused variable
    
    //Serial.begin(9600);
    pinMode(REEDPIN, INPUT_PULLUP);

    // Subscribing to weather data (feels like temp, cloud data)
	Particle.subscribe("Feels_like_temperature", feelsHandler, ALL_DEVICES);
	Particle.subscribe("Cloud_Information", cloudHandler, ALL_DEVICES);
	Particle.subscribe("Wind_Speed", windHandler, ALL_DEVICES);
	
    // Set timeout for BLE to 500ms
    BLE.setScanTimeout(SCAN_TIMEOUT_10MS);
}

void feelsHandler(const char *event, const char *data) {
    String feels_data = data;
    
    float feels_data_f = feels_data.toFloat() - 273.0;  // Converting to float
    feels_data_fah = feels_data_f * (9/5) + 32;   // Converting to fahrenheit   
}

void cloudHandler(const char *event, const char *data) {
    first_read = true;
    cloud_data = data;
}

void windHandler(const char *event, const char *data) {
    String temp_data = data;
    wind_data = temp_data.toFloat();
}

void loop() {
    
    rain = false;
    closet_open = digitalRead(REEDPIN);
    
    if(first_read==true){
        if ((closet_open == 0) && (prev_closet_open == 1)) {
            Particle.publish("clothing", String(6), PRIVATE);
            Particle.publish("umbrella found", String(2), PRIVATE);
            
        }
        else if((closet_open == 1) && (prev_closet_open == 0)){
            
            Log.trace("Closet door opened");
            
            Log.trace(cloud_data);
            
            if (strstr(cloud_data, "rain") || strstr(cloud_data, "Rain") || strstr(cloud_data, "overcast")) {
                 
                 rain = true;
                 // Check for scan start
                checkForScanStart();
            
                // Publish the RSSI and Device Info
                Particle.publish("umbrella found", String(umbrella_found), PRIVATE);
        
                // Process the publish event immediately
                Particle.process();
            }
            
            if(rain == true){
                
                Particle.publish("clothing", String(5), PRIVATE);
                
            }else{
                
                if(feels_data_fah <= 40)
                     Particle.publish("clothing", String(1), PRIVATE);
                     
                else if((feels_data_fah > 40) && (feels_data_fah <= 65))
                     Particle.publish("clothing", String(2), PRIVATE);
                
                else if(wind_data > 5.0)
                     Particle.publish("clothing", String(3), PRIVATE);
                
                else
                    Particle.publish("clothing", String(4), PRIVATE);
            
            }
        
        }
        prev_closet_open = closet_open;
    }
    else{
        Log.trace("Waiting for first cloud data");
    }
}

// Callback when a new device is found advertising
void scanResultCallback(const BleScanResult *scanResult, void *context) {

    // Collect the uuids showing in the advertising data
    BleUuid uuids[4];
    int uuidsAvail = scanResult->advertisingData.serviceUUID(uuids,sizeof(uuids)/sizeof(BleUuid));
    Log.trace("uuidsAvail = %d", uuidsAvail);
    // Print out mac info
    BleAddress addr = scanResult->address;

    // Loop over all available UUIDs
    for(int i = 0; i < uuidsAvail; i++){
        
         Log.trace("UUID: %x", uuids[i].shorted());
          int RSSI = scanResult->rssi;
            
        // Print out the UUID we're looking for
        if( (uuids[i].shorted() == BEACON_UUID) && (RSSI > -70) ) {
            
            Log.trace("MAC: %02X:%02X:%02X:%02X:%02X:%02X", addr[0], addr[1], addr[2], addr[3], addr[4], addr[5]);
            Log.trace("UUID: %x", uuids[i].shorted());

            umbrella_found = true;
        
            // Save info
            lastSeen = millis();
            lastRSSI = scanResult->rssi;
            Particle.publish("RSSI", String(lastRSSI), PRIVATE);

            // Stop scanning
            BLE.stopScanning();

            return;
        }
    }
    
    // Stop scanning
    BLE.stopScanning();

}

void checkForScanStart() {
    // Reset timer on overflow
    if( lastSeen > millis() ) {
        lastSeen = 0;
    }

    // Scan for devices
    if( (millis() > lastSeen + BEACON_RE_CHECK_MS) ){
        umbrella_found = false;
        int i = 0;
        while(i < 2){
            Log.trace("scan start: %d", i);
            BLE.scan(scanResultCallback, NULL);
            i++;
        }
    }
}

