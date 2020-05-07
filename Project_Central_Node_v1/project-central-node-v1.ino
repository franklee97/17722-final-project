// This #include statement was automatically added by the Particle IDE.
#include <ArduinoJson.h>

#define HOOK_RESP		"hook-response/get_weather_data"
#define HOOK_PUB		"weather_hook"
#define DEFAULT_CITY	"\"mycity\":\"Pittsburgh,PA\""	// Change to desired default city,state
#define API_KEY			"\"apikey\":\"91d0aaaaf5116fa8d9bc45fefb039e6d\""
#define UNITS			"\"units\":\"metric\""		// Change to "imperial" for farenheit units



static char *final_data = "hello";
void setup() {
  // Subscribe to the integration response event
  Serial.begin(9600);
  Serial.println("Begin");
  Particle.subscribe(HOOK_RESP, myHandler, MY_DEVICES);
  Serial.println("Subscribe success");
}

void myHandler(const char *event, const char *data) {
  // Handle the integration response
  Serial.println("In Handler");
  StaticJsonDocument<500> doc;
  deserializeJson(doc, data);
  float current_temp = doc["main"]["temp"];
  float feels_like = doc["main"]["feels_like"];
  float pressure = doc["main"]["pressure"];
  float humidity = doc["main"]["humidity"];
  float wind_speed = doc["wind"]["speed"];
  const char * cloud_data = doc["weather"][0]["description"];
  Particle.publish("Current_temperature", String(current_temp), MY_DEVICES);
  Particle.publish("Feels_like_temperature", String(feels_like), MY_DEVICES);
  //Particle.publish("Pressure", String(pressure), MY_DEVICES);
  //Particle.publish("Humidity", String(humidity), MY_DEVICES);
  Particle.publish("Wind_Speed", String(wind_speed), MY_DEVICES);
  Particle.publish("Cloud_Information", cloud_data, MY_DEVICES);
  delay(2000);
}
          


void loop() {
  // Get some data
  String data = String(10);
  // Trigger the integration
  Particle.publish("get_weather_data", data, PRIVATE);
  // Wait 60 seconds
  delay(60000);

}
      


// http://api.openweathermap.org/data/2.5/forecast?id=5206379&APPID=46d370b2ee41b1846de77abf17e2b1e6 second implementation

// http://api.openweathermap.org/data/2.5/weather?q=Pittsburgh&appid=46d370b2ee41b1846de77abf17e2b1e6 first one