/*
 * Copyright (c) 2000-2011 Niclas Hedhman.
 *
 * Licensed  under the  Apache License, Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.color4j.exports.cxf;

import org.color4j.exports.TextFileReflectanceExporter;

/**
 * contains constants needed for a CXF XML document;  manually taken from Macbeth's
 * whitepapers on the CXF format: a DTD was given in the white papers. comments denote
 * dependency and use:
 * ? == once or not at all, + == at least once, * == 0 or more,
 * no value implies one and only one is needed
 *
 * we don't actually use all these values.  here for future use (heh).
 *
 * 12/15/2004: KH  -  the DTD as of this date is located at the bottom of this interface
 */
public interface CXFElementTagNames
{
    String ROOT_ELEMENT = TextFileReflectanceExporter.CXF;

    //ROOT_ELEMENT children
    String VERSION = "Version";
    String NAME = "Name";    //PCDATA; string text
    String DESCRIPTION = "Description";    //? - PCDATA; string text
    String SAMPLE_SET = "SampleSet";     //* 
    String CONDITIONS = "Conditions";    //*
    String ICC_PROFILE = "ICC-Profile";    //*
    String BINARY_OBJECT = "BinaryObject";    //* - PCDATA; binary data in the MIME format

    //SAMPLE_SET children: 
    String SAMPLE = "Sample";  //+

    //CONDITIONS children
    String ID = "ID";    //PCDATA;  some unique identifier
    String ATTRIBUTE = "Attribute";    //+ - PCDATA; some value corresponding to the NAME CDATA
    //***NOTE:  conditions for spectral data need the specific following attribute names and values:
    String MAX_LAMBDA = "Physical.WavelengthSpectrumMax";  //some number with nm suffix
    String FILTER_USED = "Physical.Filter";   //Yes or No
    String DATA_POINTS = "Physical.NumberOfDataPointsSpectrum"; //an integer
    String MIN_LAMBDA = "Physical.WavelengthSpectrumMin";

    //ICC-PROFILE children
    String ICC_DATA = "ICC-Data"; //<empty>:  ICC-File MIME-encoding

    //BINARY_OBJECT children
    String MIME_TYPE = "MIME-Type"; // CDATA, IMPLIED; legal MIME-type(?)

    //SAMPLE children
    String SAMPLE_ATTRIBUTE = "SampleAttribute";
    String BINARY_DATA = "BinaryData";   //*
    String SPECTRUM = "Spectrum";   //*
    String COLOR_VECTOR = "ColorVector";    //*
    String DEVICE_COLOR = "DeviceColor";    //*
    String NAMED_COLOR = "NamedColor";    //*
    String DENSITY = "Density";    //*

    //BINARY_DATA children
    String UNIQUE_ID = "UniqueID";    //CDATA, IMPLIED; refers to some BINARY_OBJECT->NAME
    String BINARY_DATA_LINK = "BinaryDataLink";  //<empty>:  not sure what goes here

    //SPECTRUM children
    String SPECTRUM_DATA = "SpectrumData";    //PCDATA;  space-separated decimal values

    //COLOR_VECTOR children
    String COLORSPACE = "ColorSpace";  //PCDATA; string name of coorspace
    String VALUE = "Value";    //+ - PCDATA; value corresponding to the NAME CDATA of Value

    //DEVICE_COLOR children
    String ICC_PROFILE_LINK = "ICC-ProfileLink";    //?

    //DENSITY children
    String DENSITY_DATA = "DensityData";    //+ - PCDATA; not sure what goes here

    //DENSITY_DATA children
    String FILTER = "Filter";    //CDATA, IMPLIED; not sure what goes here    

    //Some Extra Labels
    String COLOR4J_DESCRIPTION = "Color4j exported reflectances";
    String SET_NAME = "Reflectances";

    //So far, unused
    String EMPTY_CONDITIONS = "EmptyConditions";
    String NO_CONDITIONS = "No_Conditions";
    String LISTED = "Provided";

    /*
         * the *.cxf DTD December 15, 2004
         *
         * <!DOCTYPE CXF [
    <!--Color eXchange Format-->
    <!ELEMENT ID  (#PCDATA) >
    <!ELEMENT Name  (#PCDATA) >
    <!ELEMENT Creator  (#PCDATA) >
    <!ELEMENT Created  (#PCDATA) >
    <!ELEMENT Modified  (#PCDATA) >
    <!ELEMENT Description  (#PCDATA) >
    <!ELEMENT AdditionalData  (Value*) >
    <!ELEMENT Conditions  (ID,Attribute*,Spectrum?) >
    <!ELEMENT Value  (#PCDATA) >
    <!ATTLIST Value
                   Name  CDATA    #IMPLIED  >
    <!ELEMENT CXF  (Name,Description?,Creator?,Created?,Modified?,AdditionalData?,SampleSet*,Conditions*,ICC-Profile*,BinaryObject*) >
    <!ATTLIST CXF
                   Version  CDATA    #REQUIRED  >
    <!ELEMENT SampleSet  (Name,Description?,Creator?,Created?,Modified?,AdditionalData?,Sample*) >
    <!ELEMENT Sample  (Name,Description?,Creator?,Created?,Modified?,AdditionalData?,SampleAttribute*) >
    <!ELEMENT SampleAttribute  (Spectrum?,ColorVector?,DeviceColor?,NamedColor?,Density?,Recipe?) >
    <!ELEMENT Spectrum  (Name?,Description?,Creator?,Created?,Modified?,AdditionalData?,Value+) >
    <!ATTLIST Spectrum
                   Conditions  CDATA    #REQUIRED  >
    <!ELEMENT ColorVector  (Name?,Description?,Creator?,Created?,Modified?,AdditionalData?,Value+) >
    <!ATTLIST ColorVector
                   Conditions  CDATA    #REQUIRED  >
    <!ELEMENT AssortmentName  (#PCDATA) >
    <!ELEMENT Thickness  (#PCDATA) >
    <!ELEMENT SubstrateLinkName  (#PCDATA) >
    <!ELEMENT Components  (Value+) >
    <!ELEMENT Recipe  (Name?,Description?,Creator?,Created?,Modified?,AdditionalData?,AssortmentName?,Thickness?,SubstrateLinkName?,Components) >
    <!ATTLIST Recipe
                   Conditions  CDATA    #REQUIRED  >
    <!ELEMENT ICC-Profile  (Name,Description?,Creator?,Created?,Modified?,AdditionalData?,ICC-Data) >
    <!ATTLIST ICC-Profile
                   Name  CDATA    #IMPLIED  >
    <!ELEMENT BinaryObject  (#PCDATA) >
    <!ATTLIST BinaryObject
                   Name  CDATA    #IMPLIED
                   MIME-Type  CDATA    #IMPLIED  >
    <!ELEMENT Attribute  (#PCDATA) >
    <!ATTLIST Attribute
                   Name  CDATA    #IMPLIED  >
    <!ELEMENT ICC-Data   EMPTY  >
    <!ELEMENT DeviceColor  (Name?,Description?,Creator?,Created?,Modified?,AdditionalData?,Value+,ICC-ProfileLink?) >
    <!ATTLIST DeviceColor
                   Conditions  CDATA    #REQUIRED  >
    <!ELEMENT NamedColor  (Name,Description?,Creator?,Created?,Modified?,AdditionalData?) >
    <!ATTLIST NamedColor
                   Conditions  CDATA    #REQUIRED  >
    <!ELEMENT Density  (Name?,Description?,Creator?,Created?,Modified?,AdditionalData?,DensityData+) >
    <!ATTLIST Density
                   Conditions  CDATA    #REQUIRED  >
    <!ELEMENT ICC-ProfileLink   EMPTY  >
    <!ATTLIST ICC-ProfileLink
                   UniqueID  CDATA    #IMPLIED  >
    <!ELEMENT DensityData  (#PCDATA) >
    <!ATTLIST DensityData
                   Filter  CDATA    #IMPLIED  >
    ]>
    */
}
