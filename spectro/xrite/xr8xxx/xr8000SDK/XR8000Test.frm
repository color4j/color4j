VERSION 5.00
Begin VB.Form frmMain 
   BorderStyle     =   1  'Fixed Single
   Caption         =   "XR8000 Instrument Interface Test Application"
   ClientHeight    =   4980
   ClientLeft      =   45
   ClientTop       =   330
   ClientWidth     =   10320
   Icon            =   "XR8000Test.frx":0000
   LinkTopic       =   "Form1"
   MaxButton       =   0   'False
   ScaleHeight     =   4980
   ScaleWidth      =   10320
   StartUpPosition =   2  'CenterScreen
   Begin VB.Frame Frame2 
      Caption         =   "Set Configuration"
      Height          =   2895
      Left            =   7920
      TabIndex        =   27
      Top             =   120
      Width           =   2295
      Begin VB.TextBox txtReflectance 
         Height          =   285
         Left            =   1440
         TabIndex        =   37
         Top             =   1560
         Width           =   495
      End
      Begin VB.TextBox txtAngle 
         Height          =   285
         Left            =   1440
         TabIndex        =   36
         Top             =   1920
         Width           =   495
      End
      Begin VB.CommandButton cmdSetConfig 
         Caption         =   "Set"
         Height          =   375
         Left            =   360
         TabIndex        =   31
         Top             =   2400
         Width           =   855
      End
      Begin VB.TextBox txtAperture 
         Height          =   285
         Left            =   1440
         TabIndex        =   30
         Top             =   480
         Width           =   495
      End
      Begin VB.TextBox txtFilter 
         Height          =   285
         Left            =   1440
         TabIndex        =   29
         Top             =   840
         Width           =   495
      End
      Begin VB.TextBox txtPctUV 
         Height          =   285
         Left            =   1440
         TabIndex        =   28
         Top             =   1200
         Width           =   495
      End
      Begin VB.Label Label10 
         Caption         =   "Reflectance"
         Height          =   255
         Left            =   360
         TabIndex        =   39
         Top             =   1560
         Width           =   975
      End
      Begin VB.Label Label9 
         Caption         =   "Angle"
         Height          =   255
         Left            =   360
         TabIndex        =   38
         Top             =   1920
         Width           =   855
      End
      Begin VB.Label Label7 
         Caption         =   "Aperture"
         Height          =   255
         Left            =   360
         TabIndex        =   35
         Top             =   480
         Width           =   855
      End
      Begin VB.Label Label6 
         Caption         =   "Filter"
         Height          =   255
         Left            =   360
         TabIndex        =   34
         Top             =   870
         Width           =   855
      End
      Begin VB.Label Label5 
         Caption         =   "Pct UV"
         Height          =   255
         Left            =   360
         TabIndex        =   33
         Top             =   1230
         Width           =   855
      End
      Begin VB.Label lblSetConfig 
         BorderStyle     =   1  'Fixed Single
         Height          =   255
         Left            =   1440
         TabIndex        =   32
         Top             =   2400
         Width           =   495
      End
   End
   Begin VB.CommandButton cmdReleaseVideo 
      Caption         =   "Release Video"
      Height          =   495
      Left            =   240
      TabIndex        =   25
      Top             =   4320
      Width           =   1695
   End
   Begin VB.PictureBox picCamera 
      Height          =   1095
      Left            =   3360
      ScaleHeight     =   1035
      ScaleWidth      =   1635
      TabIndex        =   24
      Top             =   3720
      Width           =   1695
   End
   Begin VB.CommandButton cmdCaptureVideo 
      Caption         =   "Capture Video"
      Enabled         =   0   'False
      Height          =   495
      Left            =   240
      TabIndex        =   22
      Top             =   3720
      Width           =   1695
   End
   Begin VB.CommandButton cmdIsReflectance 
      Caption         =   "Is Reflectance"
      Height          =   495
      Left            =   240
      TabIndex        =   20
      Top             =   3120
      Width           =   1695
   End
   Begin VB.TextBox txtRefls 
      Height          =   2895
      Left            =   5280
      MultiLine       =   -1  'True
      ScrollBars      =   2  'Vertical
      TabIndex        =   19
      Top             =   1920
      Width           =   2415
   End
   Begin VB.CommandButton cmdGetModel 
      Caption         =   "GetModel"
      Height          =   495
      Left            =   3360
      TabIndex        =   17
      Top             =   1320
      Width           =   1695
   End
   Begin VB.CommandButton cmdGetHiWavelength 
      Caption         =   "Get High Wavelength"
      Height          =   495
      Left            =   240
      TabIndex        =   15
      Top             =   2520
      Width           =   1695
   End
   Begin VB.CommandButton cmdGetConfigString 
      Caption         =   "Get Config String"
      Height          =   495
      Left            =   3360
      TabIndex        =   10
      Top             =   120
      Width           =   1695
   End
   Begin VB.CommandButton cmdGetSerialNo 
      Caption         =   "Get Serial Number"
      Height          =   495
      Left            =   3360
      TabIndex        =   9
      Top             =   720
      Width           =   1695
   End
   Begin VB.CommandButton cmdCalibrate 
      Caption         =   "Calibrate"
      Height          =   495
      Left            =   3360
      TabIndex        =   8
      Top             =   3120
      Width           =   1695
   End
   Begin VB.CommandButton cmdConfigure 
      Caption         =   "Configure"
      Height          =   495
      Left            =   3360
      TabIndex        =   7
      Top             =   2520
      Width           =   1695
   End
   Begin VB.CommandButton cmdGetReflectances 
      Caption         =   "Get Reflectances"
      Height          =   495
      Left            =   3360
      TabIndex        =   6
      Top             =   1920
      Width           =   1695
   End
   Begin VB.CommandButton cmdGetLoWavelength 
      Caption         =   "Get Lo Wavelength"
      Height          =   495
      Left            =   240
      TabIndex        =   5
      Top             =   1920
      Width           =   1695
   End
   Begin VB.CommandButton cmdStandalone 
      Caption         =   "Standalone"
      Height          =   495
      Left            =   240
      TabIndex        =   2
      Top             =   120
      Width           =   1695
   End
   Begin VB.CommandButton cmdMeasure 
      Caption         =   "Measure"
      Height          =   495
      Left            =   240
      TabIndex        =   1
      Top             =   1320
      Width           =   1695
   End
   Begin VB.CommandButton cmdInitialize 
      Caption         =   "Initialize"
      Height          =   495
      Left            =   240
      TabIndex        =   0
      Top             =   720
      Width           =   1695
   End
   Begin VB.Label lblReleaseVideo 
      BorderStyle     =   1  'Fixed Single
      Height          =   255
      Left            =   2160
      TabIndex        =   26
      Top             =   4440
      Width           =   855
   End
   Begin VB.Label lblCaptureVideo 
      BorderStyle     =   1  'Fixed Single
      Height          =   255
      Left            =   2160
      TabIndex        =   23
      Top             =   3840
      Width           =   855
   End
   Begin VB.Label lblIsReflectance 
      BorderStyle     =   1  'Fixed Single
      Height          =   255
      Left            =   2160
      TabIndex        =   21
      Top             =   3240
      Width           =   855
   End
   Begin VB.Label lblModel 
      BorderStyle     =   1  'Fixed Single
      Height          =   255
      Left            =   5280
      TabIndex        =   18
      Top             =   1440
      Width           =   2415
   End
   Begin VB.Label lblHiWavelength 
      BorderStyle     =   1  'Fixed Single
      Height          =   255
      Left            =   2160
      TabIndex        =   16
      Top             =   2640
      Width           =   855
   End
   Begin VB.Label lblMeasure 
      BorderStyle     =   1  'Fixed Single
      Height          =   255
      Left            =   2160
      TabIndex        =   14
      Top             =   1440
      Width           =   855
   End
   Begin VB.Label lblSerialNo 
      BorderStyle     =   1  'Fixed Single
      Height          =   255
      Left            =   5280
      TabIndex        =   13
      Top             =   840
      Width           =   2415
   End
   Begin VB.Label lblLoWavelength 
      BorderStyle     =   1  'Fixed Single
      Height          =   255
      Left            =   2160
      TabIndex        =   12
      Top             =   2040
      Width           =   855
   End
   Begin VB.Label lblConfigString 
      BorderStyle     =   1  'Fixed Single
      Height          =   255
      Left            =   5280
      TabIndex        =   11
      Top             =   240
      Width           =   2415
   End
   Begin VB.Label lblInitialize 
      BorderStyle     =   1  'Fixed Single
      Height          =   255
      Left            =   2160
      TabIndex        =   4
      Top             =   840
      Width           =   855
   End
   Begin VB.Label lblStandalone 
      BorderStyle     =   1  'Fixed Single
      Height          =   255
      Left            =   2160
      TabIndex        =   3
      Top             =   240
      Width           =   855
   End
End
Attribute VB_Name = "frmMain"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = True
Attribute VB_Exposed = False
Option Explicit

Dim nOK As Integer

Private Sub cmdCalibrate_Click()
    VB_Instrument_Calibrate
End Sub

Private Sub cmdCaptureVideo_Click()
    Dim hWnd As Long
    hWnd = picCamera.hWnd
    lblCaptureVideo = ""
    nOK = VB_Instrument_CaptureVideo(hWnd)
    lblCaptureVideo = nOK
End Sub

Private Sub cmdConfigure_Click()
    VB_Instrument_Configure
End Sub

Private Sub cmdGetConfigString_Click()
    Dim sData As String * 50
    lblConfigString = ""
    VB_Instrument_GetConfigString sData
    lblConfigString = sData
End Sub

Private Sub cmdGetHiWavelength_Click()
    Dim iHi As Integer
    lblHiWavelength = ""
    iHi = VB_Instrument_GetHiWavelength()
    lblHiWavelength = iHi
End Sub

Private Sub cmdGetLoWavelength_Click()
    Dim iLo As Integer
    lblLoWavelength = ""
    iLo = VB_Instrument_GetLoWavelength()
    lblLoWavelength = iLo
End Sub

Private Sub cmdGetModel_Click()
    Dim sData As String * 50
    lblModel = ""
    VB_Instrument_GetModel sData
    lblModel = sData
End Sub

Private Sub cmdGetReflectances_Click()
    
    Dim Refls(38) As Single
    Dim iAngle As Integer
    
    ' Figure out whether we have included or excluded data
    Dim sData As String * 50
    VB_Instrument_GetConfigString sData
    If Mid(sData, 2, 1) = "I" Then
        iAngle = 0
    Else
        iAngle = 1
    End If
    
    txtRefls = ""
    
    ' Get the data
    Call VB_Instrument_GetReflectances(Refls(0), iAngle)
    
    ' Display the data to the user
    Dim i As Integer
    For i = 0 To 38
        txtRefls = txtRefls & Str(i * 10 + 360) & "=" & Refls(i) & vbCrLf
    Next
    
End Sub

Private Sub cmdGetSerialNo_Click()
    Dim sData As String * 50
    lblSerialNo = ""
    VB_Instrument_GetSerialNo sData
    lblSerialNo = sData
End Sub

Private Sub cmdInitialize_Click()
    lblInitialize = ""
    nOK = VB_Instrument_Initialize()
    lblInitialize = nOK

    Dim MyControl As Control
    For Each MyControl In frmMain.Controls
        If TypeOf MyControl Is CommandButton Then
            MyControl.Enabled = True
        End If
    Next

End Sub

Private Sub cmdIsReflectance_Click()
    lblIsReflectance = ""
    nOK = VB_Instrument_IsReflectance()
    lblIsReflectance = nOK
End Sub

Private Sub cmdMeasure_Click()
    lblMeasure = ""
    nOK = VB_Instrument_Measure()
    lblMeasure = nOK
End Sub

Private Sub cmdReleaseVideo_Click()
    lblReleaseVideo = ""
    nOK = VB_Instrument_ReleaseVideo()
    lblReleaseVideo = nOK
End Sub

Private Sub cmdSetConfig_Click()
    
    Dim nAperture As Integer
    Dim nFilter As Integer
    Dim fPctEnergy As Single
    Dim nReflectance As Integer
    Dim nAngle As Integer
    
    nAperture = Val(txtAperture)
    nFilter = Val(txtFilter)
    fPctEnergy = Val(txtPctUV)
    nReflectance = Val(txtReflectance)
    nAngle = Val(txtAngle)
    
    Call VB_Instrument_SetConfiguration(nAperture, nFilter, fPctEnergy, nReflectance, nAngle)
    
End Sub

Private Sub cmdStandalone_Click()
    lblStandalone = ""
    nOK = VB_Instrument_Standalone()
    lblStandalone = nOK

    Dim MyControl As Control
    For Each MyControl In frmMain.Controls
        If TypeOf MyControl Is CommandButton Then
            MyControl.Enabled = True
        End If
    Next

End Sub

Private Sub Form_Load()
    
    ChDir App.Path
    
    Dim MyControl As Control
    For Each MyControl In frmMain.Controls
        If TypeOf MyControl Is CommandButton Then
            MyControl.Enabled = False
        End If
    Next
    cmdInitialize.Enabled = True
    cmdStandalone.Enabled = True

End Sub
