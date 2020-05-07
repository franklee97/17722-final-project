#define REEDPIN 4

static int connected = 1; // 0 = connected; 1 = disconnected
static int prev_connected = 1;
String window_status = "Not opened";
String window_todo = "Opened: close the window";


float feels_data_fah = 0.0;
String cloud_data;

void setup() {
	Serial.begin(9600);
	pinMode(REEDPIN, INPUT_PULLUP);
	
	// Subscribing to weather data (feels like temp, cloud data)
	Particle.subscribe("Feels_like_temperature", feelsHandler, ALL_DEVICES);
	Particle.subscribe("Cloud_Information", cloudHandler, ALL_DEVICES);
	
	
	Serial.println("Simple reed switch");
	Serial.println();
}

void feelsHandler(const char *event, const char *data) {
    String feels_data = data;
    float feels_data_f = feels_data.toFloat();  // Converting to float
    

    feels_data_fah = (feels_data_f - 273.0) * (9.0/5.0) + 32.0;   // Converting to fahrenheit
    Serial.println(feels_data_fah);
}

void cloudHandler(const char *event, const char *data) {
    cloud_data = data;
}


void loop() {
    connected = digitalRead(REEDPIN);
    
    if (connected != prev_connected)
    {
        connected ? window_status = "Opened" : window_status = "Not opened";

        // If temp is below 50 or rain or broken cloud, publish new data
        if (window_status.equals("Opened")) {
            if (strstr(cloud_data, "rain") || strstr(cloud_data, "Rain") || strstr(cloud_data, "overcast")) {
                window_todo = "Opened: close the window";
            }
            else if (feels_data_fah < 50.0) {
                window_todo = "Opened: winter";
            }
            else {
                window_todo = "Opened: don't close the window";
            }
        }
        else {
            window_todo = "Closed";
        }
        
        
        Particle.publish("window_todo", window_todo, PUBLIC);
        Serial.println(feels_data_fah);
        Serial.println(cloud_data);
        Serial.println(window_todo);
    }
    //Serial.println(window_todo);
    prev_connected = connected;
}