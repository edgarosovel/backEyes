#include <SoftwareSerial.h>

double distancia1, distancia2;
double tiempo1, tiempo2;
SoftwareSerial BT(10,11); //RX, TX

void setup(){
  //BLUETOOTH
  BT.begin(9600);
  Serial.begin(9600);
  //Sensor 1
  pinMode(9, OUTPUT); //pin 9 salida pulso ultasonico
  pinMode(8, INPUT); //pin 8 entrada pulso
  //Sensor 2
  pinMode(3, OUTPUT); //pin 6 salida pulso ultasonico
  pinMode(2, INPUT); //pin 7 entrada pulso
}

void loop(){
  distancia1= sonar(8,9);
  distancia2= sonar(2,3);
  Serial.println(distancia1);
  BT.print(distancia1);
  BT.print("a");
  BT.print(distancia2);
  BT.println();
  //delay(50);
}

double sonar(int input, int output){
  digitalWrite(output,LOW);
  delayMicroseconds(5);
  digitalWrite(output,HIGH);
  delayMicroseconds(10);
  double tiempo=pulseIn(input, HIGH);
  return getDistancia(tiempo);
}

double getDistancia(double tiempo){
  return (tiempo/(29.2*2));
}
