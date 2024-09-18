#include "BluetoothSerial.h"
#include <ESP32Servo.h>

#define INIT_CLOSE 1

#define MODULO_A_PS 12
#define MODULO_A_PC 14
#define MODULO_B_PS 27
#define MODULO_B_PC 26
#define MODULO_C_PS 25
#define MODULO_C_PC 33
#define MODULO_D_PS 32
#define MODULO_D_PC 23
#define MODULO_E_PS 4
#define MODULO_E_PC 21
#define MODULO_F_PS 15
#define MODULO_F_PC 2

#define MODULO_LED1 16
#define MODULO_LED2 17

#define LED_ON 0
#define LED_OFF 1

#define DISPENSER_QTY 6
#define DISPENSER_STOCK 50


#define SERVO_MIN 500
#define SERVO_MAX 2400

#define DOOR_CLOSE 45
#define DOOR_OPEN DOOR_CLOSE + 80
#define DOOR_ZERO 0

#define RELEASE_DOORS 0x01
#define CONTROL_DOORS 0x02

#define DELAY_DROP 500
#define DELAY_BLINK 500
#define DELAY_WAIT 250

#define RESET_RELEASE 'memset (release, 0x00, sizeof(release))'

Servo servo_A_ps;
Servo servo_A_pc;
Servo servo_B_ps;
Servo servo_B_pc;
Servo servo_C_ps;
Servo servo_C_pc;
Servo servo_D_ps;
Servo servo_D_pc;
Servo servo_E_ps;
Servo servo_E_pc;
Servo servo_F_ps;
Servo servo_F_pc;

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

  digitalWrite(MODULO_LED1, LED_ON);
  digitalWrite(MODULO_LED2, LED_ON);


  servo_A_ps.attach(MODULO_A_PS, SERVO_MIN, SERVO_MAX);
  servo_A_pc.attach(MODULO_A_PC, SERVO_MIN, SERVO_MAX);
  servo_B_ps.attach(MODULO_B_PS, SERVO_MIN, SERVO_MAX);
  servo_B_pc.attach(MODULO_B_PC, SERVO_MIN, SERVO_MAX);
  servo_C_ps.attach(MODULO_C_PS, SERVO_MIN, SERVO_MAX);
  servo_C_pc.attach(MODULO_C_PC, SERVO_MIN, SERVO_MAX);
  servo_D_ps.attach(MODULO_D_PS, SERVO_MIN, SERVO_MAX);
  servo_D_pc.attach(MODULO_D_PC, SERVO_MIN, SERVO_MAX);
  servo_E_ps.attach(MODULO_E_PS, SERVO_MIN, SERVO_MAX);
  servo_E_pc.attach(MODULO_E_PC, SERVO_MIN, SERVO_MAX);
  servo_F_ps.attach(MODULO_F_PS, SERVO_MIN, SERVO_MAX);
  servo_F_pc.attach(MODULO_F_PC, SERVO_MIN, SERVO_MAX);

#ifdef INIT_CLOSE
  servo_A_ps.write(DOOR_CLOSE);
  servo_A_pc.write(DOOR_CLOSE);
  servo_B_ps.write(DOOR_CLOSE);
  servo_B_pc.write(DOOR_CLOSE);
  servo_C_ps.write(DOOR_CLOSE);
  servo_C_pc.write(DOOR_CLOSE);
  servo_D_ps.write(DOOR_CLOSE);
  servo_D_pc.write(DOOR_CLOSE);
  servo_E_ps.write(DOOR_CLOSE);
  servo_E_pc.write(DOOR_CLOSE);
  servo_F_ps.write(DOOR_CLOSE);
  servo_F_pc.write(DOOR_CLOSE);
#else
  servo_A_ps.write(DOOR_OPEN);
  servo_A_pc.write(DOOR_OPEN);
  servo_B_ps.write(DOOR_OPEN);
  servo_B_pc.write(DOOR_OPEN);
  servo_C_ps.write(DOOR_OPEN);
  servo_C_pc.write(DOOR_OPEN);
  servo_D_ps.write(DOOR_OPEN);
  servo_D_pc.write(DOOR_OPEN);
  servo_E_ps.write(DOOR_OPEN);
  servo_E_pc.write(DOOR_OPEN);
  servo_F_ps.write(DOOR_OPEN);
  servo_F_pc.write(DOOR_OPEN);
#endif

  RESET_RELEASE;

  Serial.begin(115200);
  SerialBT.begin(device_name);
  SerialBT.printf("The device with name \"%s\" is started.\nNow you can pair it with Bluetooth!\n", device_name.c_str());

  delay(DELAY_DROP * 2);
  digitalWrite(MODULO_LED2, LED_OFF);
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
        //SerialBT.print("item: ");
        //SerialBT.println(item,HEX);
        //SerialBT.print("release[int_releases]: ");
        //SerialBT.println(release[int_releases],HEX);

        //SerialBT.print("AND: ");
        //SerialBT.println(release[int_releases] & (0x01<<(item-1)),HEX);

        while (0 != (release[int_releases] & (0x01 << (item - 1)))) {
          int_releases++;

          //SerialBT.print("release[int_releases]: ");
          //SerialBT.println(release[int_releases],HEX);
        }
        release[int_releases] |= 0x01 << (item - 1);

        //SerialBT.print("release[");
        //SerialBT.print(int_releases,DEC);
        //SerialBT.print("]: ");
        //SerialBT.println(item,release[int_releases]);


        if (int_releases > releases) releases = int_releases;

        //SerialBT.print("(");
        //SerialBT.print(i,DEC);
        //SerialBT.print("/");
        //SerialBT.print(qty,DEC);
        //SerialBT.print(") ");
        //RecebeComando(buffer[i]);
        break;
      case 'r':
        SetDoorState(DOOR_CLOSE, RELEASE_DOORS);
        return;
      case 't':
        SetDoorState(DOOR_CLOSE, CONTROL_DOORS);
        return;
      case 'R':
        SetDoorState(DOOR_OPEN, RELEASE_DOORS);
        return;
      case 'T':
        SetDoorState(DOOR_OPEN, CONTROL_DOORS);
        return;
      case 'z':
        SetDoorState(DOOR_ZERO, CONTROL_DOORS | RELEASE_DOORS);
        return;
      default:
        SerialBT.print("Sabor desconhecido... ");
        SerialBT.println(buffer[i]);
        break;
    }
  }

  
  Serial.print("Ri");
  Serial.print(int_releases,DEC);
  Serial.print("\n");
  for (item = 0; release[item] && (item < int_releases) && (item < DISPENSER_STOCK); item++) {
    Serial.print("ri");
    Serial.print(item,DEC);
    Serial.print("\n");
    SerialBT.print("release[");
    SerialBT.print(item, DEC);
    SerialBT.print("]: ");
    SerialBT.println(release[item], HEX);
    Release(release[item]);
    release[item] = 0x00;
    Serial.print("ri");
    Serial.print(item,DEC);
    Serial.print("\n");
  }
  Serial.print("Rf");
  Serial.print(int_releases,DEC);
  Serial.print("\n");

  SerialBT.println("--- FIM ---");
}

void Release(char comando) {

  digitalWrite(MODULO_LED2, LED_ON);
  if (comando & 0x01) { servo_A_pc.write(DOOR_OPEN); Serial.print("Ai\n");}
  if (comando & 0x02) { servo_B_pc.write(DOOR_OPEN); Serial.print("Bi\n");}
  if (comando & 0x04) { servo_C_pc.write(DOOR_OPEN); Serial.print("Ci\n");}
  if (comando & 0x08) { servo_D_pc.write(DOOR_OPEN); Serial.print("Di\n");}
  if (comando & 0x10) { servo_E_pc.write(DOOR_OPEN); Serial.print("Ei\n");}
  if (comando & 0x20) { servo_F_pc.write(DOOR_OPEN); Serial.print("Fi\n");}

  delay(DELAY_DROP);

  if (comando & 0x01) { servo_A_pc.write(DOOR_CLOSE); }
  if (comando & 0x02) { servo_B_pc.write(DOOR_CLOSE); }
  if (comando & 0x04) { servo_C_pc.write(DOOR_CLOSE); }
  if (comando & 0x08) { servo_D_pc.write(DOOR_CLOSE); }
  if (comando & 0x10) { servo_E_pc.write(DOOR_CLOSE); }
  if (comando & 0x20) { servo_F_pc.write(DOOR_CLOSE); }

  delay(DELAY_WAIT);

  if (comando & 0x01) { servo_A_ps.write(DOOR_OPEN); }
  if (comando & 0x02) { servo_B_ps.write(DOOR_OPEN); }
  if (comando & 0x04) { servo_C_ps.write(DOOR_OPEN); }
  if (comando & 0x08) { servo_D_ps.write(DOOR_OPEN); }
  if (comando & 0x10) { servo_E_ps.write(DOOR_OPEN); }
  if (comando & 0x20) { servo_F_ps.write(DOOR_OPEN); }

  digitalWrite(MODULO_LED2, LED_OFF);
  delay(DELAY_DROP);

  if (comando & 0x01) { servo_A_ps.write(DOOR_CLOSE); Serial.print("Af\n");}
  if (comando & 0x02) { servo_B_ps.write(DOOR_CLOSE); Serial.print("Bf\n");}
  if (comando & 0x04) { servo_C_ps.write(DOOR_CLOSE); Serial.print("Cf\n");}
  if (comando & 0x08) { servo_D_ps.write(DOOR_CLOSE); Serial.print("Df\n");}
  if (comando & 0x10) { servo_E_ps.write(DOOR_CLOSE); Serial.print("Ef\n");}
  if (comando & 0x20) { servo_F_ps.write(DOOR_CLOSE); Serial.print("Ff\n");}
}

void SetDoorState(int state, int doors) {
  if (doors == RELEASE_DOORS) {
    servo_A_ps.write(state);
    servo_B_ps.write(state);
    servo_C_ps.write(state);
    servo_D_ps.write(state);
    servo_E_ps.write(state);
    servo_F_ps.write(state);
    digitalWrite(MODULO_LED1, LED_OFF);
    digitalWrite(MODULO_LED2, LED_ON);
    delay(DELAY_BLINK);
    digitalWrite(MODULO_LED1, LED_ON);
    digitalWrite(MODULO_LED2, LED_OFF);
    delay(DELAY_BLINK);
    digitalWrite(MODULO_LED1, LED_OFF);
    digitalWrite(MODULO_LED2, LED_ON);
    delay(DELAY_BLINK);
    digitalWrite(MODULO_LED1, LED_ON);
    digitalWrite(MODULO_LED2, LED_OFF);
    delay(DELAY_BLINK);
    digitalWrite(MODULO_LED1, LED_OFF);
    digitalWrite(MODULO_LED2, LED_ON);
    delay(DELAY_BLINK);
    digitalWrite(MODULO_LED1, LED_ON);
    digitalWrite(MODULO_LED2, LED_OFF);
  }

  if (doors == CONTROL_DOORS) {
    servo_A_pc.write(state);
    servo_B_pc.write(state);
    servo_C_pc.write(state);
    servo_D_pc.write(state);
    servo_E_pc.write(state);
    servo_F_pc.write(state);

    digitalWrite(MODULO_LED1, LED_OFF);
    digitalWrite(MODULO_LED2, LED_ON);
    delay(DELAY_BLINK);
    digitalWrite(MODULO_LED1, LED_ON);
    digitalWrite(MODULO_LED2, LED_OFF);
    delay(DELAY_BLINK);
    digitalWrite(MODULO_LED1, LED_OFF);
    digitalWrite(MODULO_LED2, LED_ON);
    delay(DELAY_BLINK);
    digitalWrite(MODULO_LED1, LED_ON);
    digitalWrite(MODULO_LED2, LED_OFF);
  }
}