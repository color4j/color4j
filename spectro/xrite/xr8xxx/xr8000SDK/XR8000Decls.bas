Attribute VB_Name = "modDeclarations"
Option Explicit

Public Declare Function VB_Instrument_Standalone Lib "xr8000.dll" () As Integer
Public Declare Function VB_Instrument_Initialize Lib "xr8000.dll" () As Integer
Public Declare Function VB_Instrument_Measure Lib "xr8000.dll" () As Integer
Public Declare Sub VB_Instrument_Configure Lib "xr8000.dll" ()
Public Declare Sub VB_Instrument_Calibrate Lib "xr8000.dll" ()
Public Declare Sub VB_Instrument_GetReflectances Lib "xr8000.dll" (ByRef pRefl As Single, ByVal iAngle As Long)
Public Declare Sub VB_Instrument_GetConfigString Lib "xr8000.dll" (ByVal pRet As String)
Public Declare Function VB_Instrument_GetLoWavelength Lib "xr8000.dll" () As Integer
Public Declare Function VB_Instrument_GetHiWavelength Lib "xr8000.dll" () As Integer
Public Declare Sub VB_Instrument_GetSerialNo Lib "xr8000.dll" (ByVal pRet As String)
Public Declare Sub VB_Instrument_GetModel Lib "xr8000.dll" (ByVal pRet As String)
Public Declare Function VB_Instrument_IsReflectance Lib "xr8000.dll" () As Integer
Public Declare Function VB_Instrument_CaptureVideo Lib "xr8000.dll" (ByVal hWnd As Long) As Integer
Public Declare Function VB_Instrument_ReleaseVideo Lib "xr8000.dll" () As Integer
Public Declare Sub VB_Instrument_SetConfiguration Lib "xr8000.dll" (ByVal nAperture As Long, ByVal nFilter As Long, ByVal fPctEnergy As Single, ByVal bReflectance As Boolean, ByVal nAngle As Long)
Public Declare Function VB_Instrument_IsDataAvailable Lib "xr8000.dll" () As Integer
Public Declare Function VB_Instrument_SetLanguage Lib "xr8000.dll" (ByVal lcid As Long) As Integer

