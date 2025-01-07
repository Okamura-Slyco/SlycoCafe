#include "BluetoothSerial.h"

#include <Adafruit_PWMServoDriver.h>

Adafruit_PWMServoDriver board1 = Adafruit_PWMServoDriver(0x40);  // called this way, it uses the default address 0x40

#define SERVOMIN 125  // this is the 'minimum' pulse length count (out of 4096)
#define SERVOMAX 625  // this is the 'maximum' pulse length count (out of 4096)

#define INIT_CLOSE 1

#define MODULO_LED1 16
#define MODULO_LED2 17

#define MODULO_VCC 5

#define LED_ON 1
#define LED_OFF 0

#define DISPENSER_QTY 6
#define DISPENSER_STOCK 50


#define SERVO_MIN 500
#define SERVO_MAX 2400

#define DOOR_CLOSE 45
#define DOOR_OPEN DOOR_CLOSE + 80
#define DOOR_ZERO 0

#define DELAY_DROP 2 * 500
#define DELAY_BLINK 500
#define DELAY_SERVO 750

#define RESET_RELEASE 'memset (release, 0x00, sizeof(release))'

#define DISPENSER(X) (1 << X)
#define DISPENSER_PWM(X, Y) ((2*DISPENSER_QTY)-1-(2 * X + Y))
#define DISPENSER_A 5
#define DISPENSER_B 4
#define DISPENSER_C 3
#define DISPENSER_D 2
#define DISPENSER_E 1
#define DISPENSER_F 0

#define CONTROL_DOOR 1
#define RELEASE_DOOR 0

#define CONTROL_DOOR_OPEN_ANGLE 75
#define CONTROL_DOOR_CLOSE_ANGLE 10
#define RELEASE_DOOR_OPEN_ANGLE 75
#define RELEASE_DOOR_CLOSE_ANGLE 10


#define CONTROL_DOOR_OPEN CONTROL_DOOR, CONTROL_DOOR_OPEN_ANGLE
#define CONTROL_DOOR_CLOSE CONTROL_DOOR, CONTROL_DOOR_CLOSE_ANGLE
#define RELEASE_DOOR_OPEN RELEASE_DOOR, RELEASE_DOOR_OPEN_ANGLE
#define RELEASE_DOOR_CLOSE RELEASE_DOOR, RELEASE_DOOR_CLOSE_ANGLE

String device_name = "Slyco Dispenser Monitor";

// Check if Bluetooth is available
#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

// Check Serial Port Profile
#if !defined(CONFIG_BT_SPP_ENABLED)
#error Serial Port Profile for Bluetooth is not available or not enabled. It is only available for the ESP32 chip.
#endif

//Modulo A primeiro da esquerda para a direita olha por traz do dispenser
//PS porta saida, libera o café para o cliente
//PC porta controle, trava o cafe, para a próxima liberação.
BluetoothSerial SerialBT;

char Comando;

const int BUFFER_SIZE = DISPENSER_STOCK * DISPENSER_QTY;
char buf[BUFFER_SIZE];

char release[DISPENSER_STOCK];

void setup() {
  pinMode(MODULO_LED1, OUTPUT);
  pinMode(MODULO_LED2, OUTPUT);
  pinMode(MODULO_VCC, OUTPUT);

  digitalWrite(MODULO_LED1, LED_ON);
  digitalWrite(MODULO_LED2, LED_ON);
  digitalWrite(MODULO_VCC, LED_ON);
  RESET_RELEASE;

  Serial.begin(115200);
  SerialBT.begin(device_name);
  SerialBT.printf("The device with name \"%s\" is started.\nNow you can pair it with Bluetooth!\n", device_name.c_str());

  board1.begin();
  board1.setPWMFreq(60);  // Analog servos run at ~60 Hz updates

  release_items("Z\n", 2);

  delay(DELAY_DROP * 2);
  //digitalWrite(MODULO_LED1, LED_OFF);
  digitalWrite(MODULO_LED2, LED_OFF);
  digitalWrite(MODULO_VCC, LED_OFF);
}


void loop() {
  if (Serial.available() > 0) {
    // read the incoming bytes:
    int rlen = Serial.readBytesUntil('\n', buf, BUFFER_SIZE);

    // prints the received data
    SerialBT.print("I received (");
    SerialBT.print(rlen, DEC);
    SerialBT.print(": ");
    for (int i = 0; i < rlen; i++)
      SerialBT.print(buf[i]);
    SerialBT.println();
    release_items(buf, rlen);
  }
  delay(DOOR_OPEN);
}

void release_items(char* buffer, int qty) {
  int i;
  SerialBT.println("--- INICIO ---");

  int int_releases = 0;
  int releases = 0;
  int item;


  for (i = 0; i < DISPENSER_STOCK; i++) release[i] = 0;

  for (i = 0; i < qty; i++) {
    switch (buffer[i]) {
      case 'A':
      case 'B':
      case 'C':
      case 'D':
      case 'E':
      case 'F':
        //case 'Z':
        item = buffer[i] - 0x40;
        int_releases = 0;

        while (0 != (release[int_releases] & (0x01 << (item - 1)))) {
          int_releases++;
        }
        release[int_releases] |= 0x01 << (item - 1);


        if (int_releases > releases) releases = int_releases;

        break;
      case 'r':
        digitalWrite(MODULO_VCC, LED_ON);
        SetDoorState(DISPENSER(DISPENSER_A) | DISPENSER(DISPENSER_B) | DISPENSER(DISPENSER_C) | DISPENSER(DISPENSER_D) | DISPENSER(DISPENSER_E) | DISPENSER(DISPENSER_F), RELEASE_DOOR_OPEN);
        delay(DELAY_SERVO);
        digitalWrite(MODULO_VCC, LED_OFF);
        return;
      case 't':
        digitalWrite(MODULO_VCC, LED_ON);
        SetDoorState(DISPENSER(DISPENSER_A) | DISPENSER(DISPENSER_B) | DISPENSER(DISPENSER_C) | DISPENSER(DISPENSER_D) | DISPENSER(DISPENSER_E) | DISPENSER(DISPENSER_F), CONTROL_DOOR_OPEN);
        delay(DELAY_SERVO);
        digitalWrite(MODULO_VCC, LED_OFF);
        return;

      case 'z':
        digitalWrite(MODULO_VCC, LED_ON);
        SetDoorState(DISPENSER(DISPENSER_A) | DISPENSER(DISPENSER_B) | DISPENSER(DISPENSER_C) | DISPENSER(DISPENSER_D) | DISPENSER(DISPENSER_E) | DISPENSER(DISPENSER_F), CONTROL_DOOR_OPEN);
        SetDoorState(DISPENSER(DISPENSER_A) | DISPENSER(DISPENSER_B) | DISPENSER(DISPENSER_C) | DISPENSER(DISPENSER_D) | DISPENSER(DISPENSER_E) | DISPENSER(DISPENSER_F), RELEASE_DOOR_OPEN);
        delay(DELAY_SERVO);
        digitalWrite(MODULO_VCC, LED_OFF);
        return;
      case 'R':
        digitalWrite(MODULO_VCC, LED_ON);
        SetDoorState(DISPENSER(DISPENSER_A) | DISPENSER(DISPENSER_B) | DISPENSER(DISPENSER_C) | DISPENSER(DISPENSER_D) | DISPENSER(DISPENSER_E) | DISPENSER(DISPENSER_F), RELEASE_DOOR_CLOSE);
        delay(DELAY_SERVO);
        digitalWrite(MODULO_VCC, LED_OFF);
        return;
      case 'T':
        digitalWrite(MODULO_VCC, LED_ON);
        SetDoorState(DISPENSER(DISPENSER_A) | DISPENSER(DISPENSER_B) | DISPENSER(DISPENSER_C) | DISPENSER(DISPENSER_D) | DISPENSER(DISPENSER_E) | DISPENSER(DISPENSER_F), CONTROL_DOOR_CLOSE);
        delay(DELAY_SERVO);
        digitalWrite(MODULO_VCC, LED_OFF);
        return;
      case 'Z':
        digitalWrite(MODULO_VCC, LED_ON);
        SetDoorState(DISPENSER(DISPENSER_A) | DISPENSER(DISPENSER_B) | DISPENSER(DISPENSER_C) | DISPENSER(DISPENSER_D) | DISPENSER(DISPENSER_E) | DISPENSER(DISPENSER_F), CONTROL_DOOR_CLOSE);
        SetDoorState(DISPENSER(DISPENSER_A) | DISPENSER(DISPENSER_B) | DISPENSER(DISPENSER_C) | DISPENSER(DISPENSER_D) | DISPENSER(DISPENSER_E) | DISPENSER(DISPENSER_F), RELEASE_DOOR_CLOSE);
        delay(DELAY_SERVO);
        digitalWrite(MODULO_VCC, LED_OFF);
        return;
      default:
        SerialBT.print("Sabor desconhecido... ");
        SerialBT.println(buffer[i]);
        break;
    }
  }

  digitalWrite(MODULO_VCC, LED_ON);

  Serial.print("Ri");
  Serial.print(int_releases, DEC);
  Serial.print("\n");
  SerialBT.print("Ri");
  SerialBT.print(int_releases, DEC);
  SerialBT.print("\n");
  for (item = 0; release[item] && (item <= int_releases) && (item < DISPENSER_STOCK); item++) {
    Serial.print("ri");
    Serial.print(item, DEC);
    Serial.print(":");
    Serial.print(release[item], BIN);
    Serial.print("\n");
    SerialBT.print("ri");
    SerialBT.print(item, DEC);
    SerialBT.print(":");
    SerialBT.print(release[item], BIN);
    SerialBT.print("\n");
    Release(release[item]);
    release[item] = 0x00;
    Serial.print("rf");
    Serial.print(item, DEC);
    Serial.print("\n");
    SerialBT.print("rf");
    SerialBT.print(item, DEC);
    SerialBT.print("\n");
  }
  Serial.print("Rf");
  Serial.print(int_releases, DEC);
  Serial.print("\n");
  SerialBT.print("Rf");
  SerialBT.print(int_releases, DEC);
  SerialBT.print("\n");

  delay(DELAY_SERVO);
  digitalWrite(MODULO_VCC, LED_OFF);

  SerialBT.println("--- FIM ---");
}

void Release(char comando) {
  int door = 0;
  digitalWrite(MODULO_LED1, LED_OFF);
  digitalWrite(MODULO_LED2, LED_ON);

  SetDoorState(comando, CONTROL_DOOR_OPEN);
  SetDoorState(comando, CONTROL_DOOR_CLOSE);

  delay(DELAY_SERVO);

  SetDoorState(comando, RELEASE_DOOR_OPEN);
  SetDoorState(comando, RELEASE_DOOR_CLOSE);

  digitalWrite(MODULO_LED1, LED_ON);
  digitalWrite(MODULO_LED2, LED_OFF);
}

int angleToPulse(int ang)  //gets angle in degree and returns the pulse width
{
  int pulse = map(ang, 0, 180, SERVOMIN, SERVOMAX);  // map angle of 0 to 180 to Servo min and Servo max
  return pulse;
}

void SetDoorState(int doors, int rel_ctl, int angle) {
  int i;

  for (i = 0; i < DISPENSER_QTY; i++) {
    if (doors & (0x01 << i)) {
      board1.setPWM(DISPENSER_PWM(i, rel_ctl), 0, angleToPulse(angle));
    }
  }
  delay(DELAY_SERVO);
}