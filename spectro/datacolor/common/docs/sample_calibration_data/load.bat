@echo off
type cont.txt

VOL >>C:\ACSCC\DISK.TXT

REM ########################################################################
REM #   BLOCK START: CORRELATION DATA                                      #
REM ########################################################################

If exist *.coe copy *.coe c:\ACSDATA

REM ########################################################################
REM #   BLOCK END: CORRELATION DATA                                        #
REM ########################################################################




REM ########################################################################
REM #   BLOCK START: INSTRUMENT CALIBRATION FILES                          #
REM ########################################################################

REM     SET FLAG TO INDICATE NO CALIBRATION FILE FOUND YET...
SET CAL_FILE_FOUND=0



REM     NOW START LOOKING FOR ALL REQUIRED CALIBRATION FILES...


REM             // SPECIAL ATTENTION MUST BE TAKEN FOR "P" AND "3" DRIVERS
REM             // DUE TO THE MANNER IN WHICH CALIBRATION FILES...

:START
IF NOT EXIST *.CAL GOTO DRIVER_1

REM             // GOT HERE, EITHER "3" OR "P" DRIVER CALIBRATION FILE...
IF NOT EXIST P*.CAL GOTO DRIVER_3
:DRIVER_P
REM                    // GOT HERE, MUST BE "P" DRIVER...
REM                             // Driver "P" = DCI INSTRUMENT
REM                             //              (*.GLS IS ASCII GLOSS FILE)
COPY P*.CAL C:\ACSDATA\CALIBP.CAL /B
COPY P*.CAL C:\ACSDATA\P*.CAL /B
IF EXIST *.GLS COPY *.GLS C:\ACSDATA\*.GLS
SET CAL_FILE_FOUND=1
GOTO DRIVER_1

REM                    // GOT HERE, MUST BE "3" DRIVER...
REM                             // Driver "3" = Color-Sensor Serial
:DRIVER_3
COPY *.CAL/B C:\ACSDATA\CALIB3.CAL /B
SET CAL_FILE_FOUND=1
GOTO DRIVER_1



REM     NOW HANDLE REST OF CALIBRATION FILES WITH "SPECIAL" FILE EXTENSIONS...


REM                             // Driver "1" =  Spectro-Sensor II
:DRIVER_1
IF NOT EXIST CALIB?.??? GOTO DRIVER_Z
COPY CALIB*.*/B C:\ACSDATA\CALIB1.CAL /B
SET CAL_FILE_FOUND=1


REM                             // Driver "Z" =  SSIR
:DRIVER_Z
IF NOT EXIST CALIR*.* GOTO DRIVER_4
COPY CALIR*.*/B C:\ACSDATA\CALIBZ.CAL /B
SET CAL_FILE_FOUND=1


REM                             // Driver "4" = Color-Sensor 0/45
:DRIVER_4
IF NOT EXIST *.4 GOTO DRIVER_5
COPY *.4/B C:\ACSDATA\CALIB4.CAL /B
SET CAL_FILE_FOUND=1


REM                             // Driver "5" = Color-Sensor Multiple
:DRIVER_5
IF NOT EXIST *.5 GOTO DRIVER_2
COPY *.5/B C:\ACSDATA\CALIB5.CAL /B
SET CAL_FILE_FOUND=1


REM                             // Driver "2" = CHROMA-SENSOR CS5
:DRIVER_2
IF NOT EXIST *.CS5 GOTO DRIVER_8
COPY *.CS5 C:\ACSDATA\CALIB2.CAL /B
SET CAL_FILE_FOUND=1


REM                             // Driver "8" = CHROMA-SENSOR CS3
:DRIVER_8
IF NOT EXIST *.CS3 GOTO DRIVER_9
COPY *.CS3 C:\ACSDATA\CALIB8.CAL /B
SET CAL_FILE_FOUND=1


REM                             // Driver "9" = Elrepho 2000, 38XX...
:DRIVER_9
IF NOT EXIST *.DC9 GOTO DRIVER_W
COPY *.DC9 C:\ACSDATA\CALIB9.CAL /B
SET CAL_FILE_FOUND=1


REM                             // Driver "W" = Gretag
:DRIVER_W
IF NOT EXIST *.GRW GOTO DRIVER_X
COPY *.GRW C:\ACSDATA\CALIBW.CAL /B
SET CAL_FILE_FOUND=1


REM                             // Driver "X" = CS5 IR
:DRIVER_X
IF NOT EXIST *.5IR GOTO DRIVER_V
COPY *.5IR C:\ACSDATA\CALIBX.CAL /B
SET CAL_FILE_FOUND=1


REM                             // Driver "V" = PCS 500
:DRIVER_V
IF NOT EXIST *.PCS GOTO DRIVER_U
COPY *.PCS C:\ACSDATA\CALIBV.CAL /B
SET CAL_FILE_FOUND=1


REM                             // Driver "U" = Spectraflash 500
:DRIVER_U
IF NOT EXIST *.SPF GOTO DRIVER_T
COPY *.SPF C:\ACSDATA\CALIBU.CAL /B
SET CAL_FILE_FOUND=1


REM                             // Driver "T" = Microflash
:DRIVER_T
IF NOT EXIST *.MF GOTO DRIVER_R
COPY *.MF C:\ACSDATA\CALIBT.CAL /B
SET CAL_FILE_FOUND=1


REM                             // Driver "R" = 3990
:DRIVER_R
IF NOT EXIST *.39 GOTO DRIVER_Q
COPY *.39 C:\ACSDATA\CALIBR.CAL /B
SET CAL_FILE_FOUND=1


REM                             // Driver "Q" = DATAFLASH 100
:DRIVER_Q
IF NOT EXIST *.DFL GOTO PM1000
COPY *.DFL C:\ACSDATA\CALIBQ.CAL /B
SET CAL_FILE_FOUND=1




REM                             // Driver "R" = PAINTMAKER 1000 (EM = EMERALD)
:PM1000
IF NOT EXIST *.EM GOTO NEXT_DVR
COPY *.EM C:\ACSDATA\CALIBQ.CAL /B
SET CAL_FILE_FOUND=1





REM             ADD NEXT INSTRUMENT HERE...
:NEXT_DVR
GOTO CHKWHITE

REM ########################################################################
REM #   BLOCK END: INSTRUMENT CALIBRATION FILES                            #
REM ########################################################################




REM ########################################################################
REM #   BLOCK START: WHITE TILE DATA                                       #
REM ########################################################################

REM             // NOW COPY WHITE TILE DATA FILES IF REQUIRED...
:CHKWHITE
REM             // Check for white tile data & skip all blocks if none...
IF NOT EXIST WHITE*.DAT  GOTO CHK_ERRS

REM ************************************************************************
REM * START ACSDATA DIRECTORY BLOCK                                        *
REM ************************************************************************
REM
REM             // Check the ACSDATA directory...
:CHK_DATA
REM             // Check for ACSDATA & skip this block if not found...
IF NOT EXIST C:\ACSDATA\*.*  GOTO CHK_WIND
IF NOT EXIST C:\ACSCC\YORN.EXE COPY YORN.EXE C:\ACSCC

REM             // ACSDATA: WHITESE.DAT and WHITESI.DAT BLOCK STARTS HERE
REM             // Check for "whitese.dat" & "whitesi.dat" on floppy...
IF NOT EXIST WHITES*.DAT  GOTO DATA_D0
:DATA_SIE
REM             // Check for "whitese.dat" & "whitesi.dat" on hard drive...
IF NOT EXIST C:\ACSDATA\WHITES*.DAT  GOTO COPYDAT1
REM             // "whitese.dat" & "whitesi.dat" exist, ask before overwriting
TYPE WHTDAT1.TXT
c:\ACSCC\yorn
IF ERRORLEVEL 2 GOTO DATA_D0
IF ERRORLEVEL 1 GOTO COPYDAT1

REM             // Copy "whitese.dat" & "whitesi.dat" to acsdata...
:COPYDAT1
ECHO.
COPY WHITES*.DAT C:\ACSDATA\WHITES*.DAT /B
GOTO DATA_D0
REM             // ACSDATA: WHITESE.DAT and WHITESI.DAT BLOCK ENDS HERE


REM             // ACSDATA: WHITED0.DAT BLOCK STARTS HERE
:DATA_D0
REM             // Check for "whited0.dat" on floppy...
IF NOT EXIST WHITED0.DAT  GOTO CHK_WIND
REM             // Check for "whited0.dat" on hard drive...
IF NOT EXIST C:\ACSDATA\WHITED0.DAT  GOTO COPYDAT2
REM             // "whited0.dat" exists, ask before overwriting...
TYPE WHTDAT2.TXT
c:\ACSCC\yorn
IF ERRORLEVEL 2 GOTO CHK_WIND
IF ERRORLEVEL 1 GOTO COPYDAT2

REM             // Copy "whitese.dat" & "whitesi.dat" to acsdata...
:COPYDAT2
ECHO.
COPY WHITED0.DAT C:\ACSDATA\WHITED0.DAT /B
GOTO CHK_WIND
REM             // ACSDATA: WHITED0.DAT BLOCK ENDS HERE

REM ************************************************************************
REM * END ACSDATA DIRECTORY BLOCK                                          *
REM ************************************************************************



REM ************************************************************************
REM * START WINDOWS DIRECTORY BLOCK                                        *
REM ************************************************************************
REM
REM             // WINDOWS: WHITESE.DAT and WHITESI.DAT BLOCK STARTS HERE
:CHK_WIND
REM             // Check for WINDOWS & skip this block if not found...
IF NOT EXIST C:\WINDOWS\*.*  GOTO CHK_ERRS
IF NOT EXIST C:\ACSCC\YORN.EXE COPY YORN.EXE C:\ACSCC

REM             // WINDOWS: WHITESE.DAT and WHITESI.DAT BLOCK STARTS HERE
REM             // Check for "whitese.dat" & "whitesi.dat" on floppy...
IF NOT EXIST WHITES*.DAT  GOTO WIND_D0
:WIND_SIE
REM             // Check for "whitese.dat" & "whitesi.dat" on hard drive...
IF NOT EXIST C:\WINDOWS\WHITES*.DAT  GOTO COPYWIN1
REM             // "whitese.dat" & "whitesi.dat" exist, ask before overwriting
TYPE WHTWIN1.TXT
c:\ACSCC\yorn
IF ERRORLEVEL 2 GOTO WIND_D0
IF ERRORLEVEL 1 GOTO COPYWIN1

REM             // Copy "whitese.dat" & "whitesi.dat" to WINDOWS...
:COPYWIN1
ECHO.
COPY WHITES*.DAT C:\WINDOWS\WHITES*.DAT /B
GOTO WIND_D0
REM             // WINDOWS: WHITESE.DAT and WHITESI.DAT BLOCK ENDS HERE


REM             // WINDOWS: WHITED0.DAT BLOCK STARTS HERE
:WIND_D0
REM             // Check for "whited0.dat" on floppy...
IF NOT EXIST WHITED0.DAT  GOTO CHK_ERRS
REM             // Check for "whited0.dat" on hard drive...
IF NOT EXIST C:\WINDOWS\WHITED0.DAT  GOTO COPYWIN2
REM             // "whited0.dat" exists, ask before overwriting...
TYPE WHTWIN2.TXT
c:\ACSCC\yorn
IF ERRORLEVEL 2 GOTO CHK_ERRS
IF ERRORLEVEL 1 GOTO COPYWIN2

REM             // Copy "whitese.dat" & "whitesi.dat" to WINDOWS...
:COPYWIN2
ECHO.
COPY WHITED0.DAT C:\WINDOWS\WHITED0.DAT /B
GOTO CHK_ERRS
REM             // WINDOWS: WHITED0.DAT BLOCK ENDS HERE

REM ************************************************************************
REM * END WINDOWS DIRECTORY BLOCK                                          *
REM ************************************************************************

REM ########################################################################
REM #   BLOCK END: WHITE TILE DATA                                         #
REM ########################################################################




REM ########################################################################
REM #   BLOCK START: ERROR CHECKING                                        #
REM ########################################################################

:CHK_ERRS
REM             CHECK FOR ERRORS, i.e. no calibration file found...
IF %CAL_FILE_FOUND% == 1  GOTO END
type cal.txt
PAUSE
GOTO END

REM ########################################################################
REM #   BLOCK END: ERROR CHECKING                                          #
REM ########################################################################


:END
ECHO.
ECHO.
ECHO.

