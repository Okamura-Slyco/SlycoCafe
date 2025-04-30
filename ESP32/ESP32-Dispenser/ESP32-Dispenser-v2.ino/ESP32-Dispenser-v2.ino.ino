//#include "BluetoothSerial.h"

#include <Adafruit_PWMServoDriver.h>

Adafruit_PWMServoDriver board1 = Adafruit_PWMServoDriver();  // called this way, it uses the default address 0x40

#define INIT_CLOSE 1

#define RELE0 0
#define RELE1 1
#define RELE2 2
#define RELE3 3

#define MODULO_LED1 RELE0
#define MODULO_LED2 RELE1

#define MODULO_VCC RELE2

#define LED_ON 0
#define LED_OFF 1

#define DISPENSER_QTY 6
#define DISPENSER_STOCK 50

#define PCA9865_OSCILLATOR_FREQUENCY 27000000
#define SERVO_PWM_FREQUENCY 50


#define SERVO_MIN_PULSE 500
#define SERVO_MAX_PULSE 2500

#define LED_MIN_PULSE 0
#define LED_MAX_PULSE 1000000 / SERVO_PWM_FREQUENCY

#define DELAY_BLINK 500
#define DELAY_SERVO 1000
#define DELAY_SERVO_STEP 30

#define RESET_RELEASE 'memset (release, 0x00, sizeof(release))'

#define DISPENSER(X) (1 << X)
#define DISPENSER_PWM(X) (2 * X)
#define LED_PWM(X) DISPENSER_PWM(X) + 1
#define DISPENSER_A 5
#define DISPENSER_B 4
#define DISPENSER_C 3
#define DISPENSER_D 2
#define DISPENSER_E 1
#define DISPENSER_F 0

#define CONTROL_DOOR 1
#define RELEASE_DOOR 0

#define DRAWER_REST 180
#define DRAWER_PUSH 0
#define MAX_SERVO_ANGLE() max(DRAWER_REST, DRAWER_PUSH)
#define MIN_SERVO_ANGLE() min(DRAWER_REST, DRAWER_PUSH)

#define ROTATION_STEP_ANGLE 10

String device_name = "Slyco Dispenser Monitor";

// Check if Bluetooth is available
//#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
//#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
//#endif

// Check Serial Port Profile
//#if !defined(CONFIG_BT_SPP_ENABLED)
//#error Serial Port Profile for Bluetooth is not available or not enabled. It is only available for the ESP32 chip.
//#endif

//Modulo A primeiro da esquerda para a direita olha por traz do dispenser
//PS porta saida, libera o café para o cliente
//PC porta controle, trava o cafe, para a próxima liberação.
//BluetoothSerial SerialBT;

char Comando;

const int BUFFER_SIZE = DISPENSER_STOCK * DISPENSER_QTY;
char buf[BUFFER_SIZE];

char release[DISPENSER_STOCK];

int servoPosition[DISPENSER_QTY] = { DRAWER_REST, DRAWER_REST, DRAWER_REST, DRAWER_REST, DRAWER_REST, DRAWER_REST };

void setup() {
  pinMode(MODULO_LED1, OUTPUT);
  pinMode(MODULO_LED2, OUTPUT);
  pinMode(MODULO_VCC, OUTPUT);

  digitalWrite(MODULO_LED1, LED_ON);
  digitalWrite(MODULO_VCC, LED_ON);
  RESET_RELEASE;

  //servoPosition

  Serial.begin(115200);
  //  SerialBT.begin(device_name);
  //  SerialBT.printf("The device with name \"%s\" is started.\nNow you can pair it with Bluetooth!\n", device_name.c_str());

  board1.begin();
  board1.setOscillatorFrequency(PCA9865_OSCILLATOR_FREQUENCY);
  board1.setPWMFreq(SERVO_PWM_FREQUENCY);  // Analog servos run at ~60 Hz updates

  release_items("Z\n", 2);

  doWow();
  digitalWrite(MODULO_VCC, LED_OFF);
}


void loop() {
  if (Serial.available() > 0) {
    int rlen = Serial.readBytesUntil('\n', buf, BUFFER_SIZE);
    
    Serial.print("RECEIVED: ");
    Serial.println((char*)buf);  // print as string

    Serial.print("Size: ");
    Serial.println(rlen, DEC);

    release_items(buf, rlen);
  }
  delay(DELAY_SERVO);
}


void release_items(char* buffer, int qty) {
  int i;
  int releases = 0;
  int item;

  // Clear previous releases
  for (i = 0; i < DISPENSER_STOCK; i++) {
    release[i] = 0;
  }

  // Build release groups
  for (i = 0; i < qty; i++) {
    switch (buffer[i]) {
      case 'A':
      case 'B':
      case 'C':
      case 'D':
      case 'E':
      case 'F': {
        item = buffer[i] - 0x40;  // 'A' -> 1, 'B' -> 2, etc.
        int groupIndex = 0;

        // Find next group with free slot for this item
        while ((release[groupIndex] & (0x01 << (item - 1))) != 0) {
          groupIndex++;
        }

        release[groupIndex] |= (0x01 << (item - 1));
        if (groupIndex + 1 > releases) {
          releases = groupIndex + 1;  // count max used groups
        }
        break;
      }

      case 'z':
        digitalWrite(MODULO_VCC, LED_ON);
        SetDoorState(DISPENSER(DISPENSER_A) | DISPENSER(DISPENSER_B) |
                     DISPENSER(DISPENSER_C) | DISPENSER(DISPENSER_D) |
                     DISPENSER(DISPENSER_E) | DISPENSER(DISPENSER_F),
                     DRAWER_PUSH);
        delay(DELAY_SERVO);
        digitalWrite(MODULO_VCC, LED_OFF);
        return;

      case 'Z':
        digitalWrite(MODULO_VCC, LED_ON);
        SetDoorState(DISPENSER(DISPENSER_A) | DISPENSER(DISPENSER_B) |
                     DISPENSER(DISPENSER_C) | DISPENSER(DISPENSER_D) |
                     DISPENSER(DISPENSER_E) | DISPENSER(DISPENSER_F),
                     DRAWER_REST);
        delay(DELAY_SERVO);
        digitalWrite(MODULO_VCC, LED_OFF);
        return;

      case 'W':
        doWow();
        return;

      default:
        break;
    }
  }

  digitalWrite(MODULO_VCC, LED_ON);

  Serial.print("Ri");
  Serial.println(releases);  // Number of release groups

  for (int groupIndex = 0; groupIndex < releases; groupIndex++) {
    if (release[groupIndex] != 0) {
      process_capsule_group(groupIndex, release[groupIndex]);
      release[groupIndex] = 0x00;
    }
  }

  Serial.print("Rf");
  Serial.println(releases);

  digitalWrite(MODULO_VCC, LED_OFF);
}


void process_capsule_group(int groupIndex, uint8_t mask) {
  Serial.print("ri");
  Serial.print(groupIndex);
  Serial.print(":");
  Serial.println(mask, BIN);

  Release(mask);  // your existing function

  Serial.print("rf");
  Serial.println(groupIndex);
}


void doWow() {
  int state = 0;
  int i = 0;
  int itCounter = 0;
  int myAngle = DRAWER_REST;
  int myStep = ROTATION_STEP_ANGLE;
  digitalWrite(MODULO_LED2, LED_ON);
  while (1) {
    switch (state) {
      case 0:  // pulse 1
        if (myAngle >= MAX_SERVO_ANGLE()) {
          myStep = -1 * ROTATION_STEP_ANGLE;
          itCounter++;
        } else if (myAngle <= MIN_SERVO_ANGLE()) {
          myStep = ROTATION_STEP_ANGLE;
        }

        myAngle += myStep;
        for (i = 0; i < DISPENSER_QTY; i++) {
          uint16_t ledPwm = angleToLedPWM(myAngle);
          board1.writeMicroseconds(LED_PWM(i), ledPwm);
        }
        if (itCounter == 4) {
          state++;
          itCounter = 0;
        }
        break;

      default:
        for (i = 0; i < DISPENSER_QTY; i++) {
          uint16_t ledPwm = angleToLedPWM(DRAWER_REST);
          board1.writeMicroseconds(LED_PWM(i), ledPwm);
        }
        digitalWrite(MODULO_LED2, LED_OFF);
        return;
        break;
    }

    delay(DELAY_SERVO_STEP);
  }
}

void Release(char comando) {
  int door = 0;
  //digitalWrite(MODULO_LED1, LED_OFF);
  digitalWrite(MODULO_LED2, LED_ON);

  SetDoorState(comando, DRAWER_PUSH);

  delay(DELAY_SERVO);

  SetDoorState(comando, DRAWER_REST);

  delay(DELAY_SERVO);

  digitalWrite(MODULO_LED1, LED_ON);
  digitalWrite(MODULO_LED2, LED_OFF);
}

uint16_t angleToMicroseconds(int ang)  //gets angle in degree and returns the pulse width
{
  uint16_t pulse = (uint16_t)map(ang, DRAWER_PUSH, DRAWER_REST, SERVO_MIN_PULSE, SERVO_MAX_PULSE);  // map angle of 0 to 180 to Servo min and Servo max
  return pulse;
}

uint16_t angleToLedPWM(int ang)  //gets angle in degree and returns the pulse width
{
  uint16_t pulse = (uint16_t)map(ang, DRAWER_PUSH, DRAWER_REST, LED_MAX_PULSE, LED_MIN_PULSE);  // map angle of 0 to 180 to Servo min and Servo max
  return pulse;
}

void SetDoorState(int doors, int angle) {
  int i;

  int myAngle = 0;
  int myStep = 0;

  for (i = 0; i < DISPENSER_QTY; i++) {
    if (doors & (0x01 << i)) {
      myAngle = servoPosition[i];
      break;
    }
  }

  if (myAngle > angle) myStep = -1 * ROTATION_STEP_ANGLE;
  else myStep = ROTATION_STEP_ANGLE;

  while (myAngle != angle) {
    myAngle += myStep;
    if (myAngle > MAX_SERVO_ANGLE()) myAngle = MAX_SERVO_ANGLE();
    else if (myAngle < MIN_SERVO_ANGLE()) myAngle = MIN_SERVO_ANGLE();

    for (i = 0; i < DISPENSER_QTY; i++) {
      if (doors & (0x01 << i)) {
        uint16_t ledPwm = angleToLedPWM(myAngle);
        board1.writeMicroseconds(DISPENSER_PWM(i), angleToMicroseconds(myAngle));
        board1.writeMicroseconds(LED_PWM(i), ledPwm);
        servoPosition[i] = myAngle;
      }
    }

    delay(DELAY_SERVO_STEP);
  }
}