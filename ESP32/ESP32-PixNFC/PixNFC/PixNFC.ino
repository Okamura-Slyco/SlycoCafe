#include <Wire.h>
#include <SPI.h>
#include <Adafruit_PN532.h>
 
// If using the breakout with SPI, define the pins for SPI communication.
#define PN532_SCK  (2)
#define PN532_MOSI (3)
#define PN532_SS   (4)
#define PN532_MISO (5)
 
// If using the breakout or shield with I2C, define just the pins connected
// to the IRQ and reset lines.  Use the values below (2, 3) for the shield!
#define PN532_IRQ   (2)
#define PN532_RESET (3)  // Not connected by default on the NFC Shield
 
// Uncomment just _one_ line below depending on how your breakout or shield
// is connected to the Arduino:
 
// Use this line for a breakout with a software SPI connection (recommended):
Adafruit_PN532 nfc(PN532_SCK, PN532_MISO, PN532_MOSI, PN532_SS);
 
// Use this line for a breakout with a hardware SPI connection.  Note that
// the PN532 SCK, MOSI, and MISO pins need to be connected to the Arduino's
// hardware SPI SCK, MOSI, and MISO pins.  On an Arduino Uno these are
// SCK = 13, MOSI = 11, MISO = 12.  The SS line can be any digital IO pin.
//Adafruit_PN532 nfc(PN532_SS);
 
// Or use this line for a breakout or shield with an I2C connection:
//Adafruit_PN532 nfc(PN532_IRQ, PN532_RESET);
 
void setup(void) {
  Serial.begin(115200);
  Serial.println("Hello!");
 
  nfc.begin();
 
  uint32_t versiondata = nfc.getFirmwareVersion();
  if (! versiondata) {
    Serial.print("Didn't find PN53x board");
    while (1); // halt
  }
  // Got ok data, print it out!
  Serial.print("Found chip PN5"); Serial.println((versiondata>>24) & 0xFF, HEX); 
  Serial.print("Firmware ver. "); Serial.print((versiondata>>16) & 0xFF, DEC); 
  Serial.print('.'); Serial.println((versiondata>>8) & 0xFF, DEC);
  
  // configure board to read RFID tags
  nfc.SAMConfig();
  
}
 
 
void loop()
{
  bool success;
  // set shield to inListPassiveTarget
  success = nfc.inListPassiveTarget();
 
  if(success) {
    Serial.println("CARD FOUND");
    uint8_t selectApdu[] = {0x00, /* CLA */
                            0xA4, /* INS */
                            0x04, /* P1 ) */
                            0x00, /* P2  */
                            0x0E, /* Lc  */
                            0x32, 0x50, 0x41, 0x59, 0x2E, 0x53, 0x59, 0x53, 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31, /* Data = Lc*/
                            0x00  /* Le */ };

     uint8_t writeBinaryApdu[] = {  0x00, /* CLA */
                                    0xA4, /* INS */
                                    0x04, /* P1 ) */
                                    0x00, /* P2  */
                                    0x0E, /* Lc  */
                                    0x32, 0x50, 0x41, 0x59, 0x2E, 0x53, 0x59, 0x53, 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31, /* Data = Lc*/
                                    0x00  /* Le */ };


    uint8_t response[255];

    memset(response, 0, sizeof(response));
    uint8_t responseLength = sizeof(response);  
     
    success = nfc.inDataExchange(selectApdu, sizeof(selectApdu), response, &responseLength);

    if(success) {

      Serial.print("Select responseLength: "); Serial.println(responseLength);
      nfc.PrintHexChar(response, responseLength);

      if ((response[0] == 0x90) && (response[1] == 00)) {
        Serial.println("Response OK! Sending Pix URI...");

        success = nfc.inDataExchange(writeBinaryApdu, sizeof(writeBinaryApdu), response, &responseLength);

          if(success) {
            Serial.print("WriteBinary responseLength: "); Serial.println(responseLength);
            nfc.PrintHexChar(response, responseLength);
          }
      }
    } 
  }
}