/*
 * Copyright (c) 2011 Niclas Hedhman.
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

package org.color4j.colorimetry.illuminants;

import org.color4j.colorimetry.Spectrum;

public class TL84 extends Spectrum
{
    static final long serialVersionUID = 1L;

    private static float[] m_Readings =
        {
            // 300-304
            // 305-309
            // 310-314
            // 315-319
            // 320-324
            // 325-329
            // 330-334
            // 335-339
            // 340-344
            // 345-349
            // 350-354
            // 355-359
            // 360-364
            // 365-369
            // 370-374
            // 375-379
            0.910f, 0.812f, 0.743f, 0.695f, 0.659f,  // 380-384
            0.630f, 0.602f, 0.572f, 0.538f, 0.500f,  // 385-389
            0.460f, 0.420f, 0.384f, 0.358f, 0.351f,  // 390-394
            0.370f, 0.427f, 0.532f, 0.701f, 0.948f,  // 395-399
            1.290f, 3.568f, 5.846f, 8.124f, 10.402f,  // 400-404
            12.680f, 10.462f, 8.244f, 6.026f, 3.808f,  // 405-409
            2.190f, 1.567f, 1.581f, 1.626f, 1.697f,  // 410-414
            1.690f, 1.600f, 1.625f, 1.561f, 1.507f,  // 415-419
            0.900f, 2.620f, 2.787f, 2.960f, 3.141f,  // 420-424
            3.330f, 3.530f, 3.742f, 3.970f, 4.218f,  // 425-429
            6.390f, 10.380f, 16.270f, 22.160f, 28.050f,  // 430-434
            33.940f, 29.578f, 25.216f, 20.854f, 16.492f,  // 435-439
            14.830f, 11.094f, 10.058f, 9.022f, 7.986f,  // 440-444
            6.850f, 6.830f, 6.889f, 6.734f, 6.667f,  // 445-449
            6.600f, 6.802f, 6.801f, 6.988f, 6.961f,  // 450-454
            7.020f, 6.965f, 6.896f, 6.814f, 6.722f,  // 455-459
            6.720f, 6.611f, 6.496f, 6.377f, 6.255f,  // 460-464
            6.130f, 6.003f, 5.873f, 5.739f, 5.602f,  // 465-469
            5.460f, 5.315f, 5.169f, 5.026f, 4.895f,  // 470-474
            4.790f, 4.730f, 4.742f, 4.865f, 5.149f,  // 475-479
            5.460f, 7.386f, 9.112f, 10.838f, 12.564f,  // 480-484
            14.290f, 14.424f, 14.558f, 14.692f, 14.826f,  // 485-489
            14.760f, 13.762f, 12.564f, 11.366f, 10.168f,  // 490-494
            8.970f, 8.120f, 7.270f, 6.420f, 5.570f,  // 495-499
            4.720f, 4.036f, 3.473f, 3.011f, 2.635f,  // 500-504
            2.330f, 2.082f, 1.881f, 1.717f, 1.582f,  // 505-509
            1.470f, 1.375f, 1.293f, 1.222f, 1.158f,  // 510-514
            1.100f, 1.048f, 1.000f, 0.958f, 0.921f,  // 515-519
            0.890f, 0.765f, 0.747f, 0.735f, 0.730f,  // 520-524
            0.830f, 1.855f, 2.232f, 2.081f, 1.624f,  // 525-529
            1.280f, 1.371f, 1.018f, 1.741f, 2.662f,  // 530-534
            5.200f, 8.577f, 12.814f, 20.331f, 30.950f,  // 535-539
            44.590f, 48.240f, 55.890f, 65.540f, 73.190f,  // 540-544
            71.840f, 67.794f, 58.748f, 49.702f, 40.656f,  // 545-549
            32.610f, 26.592f, 20.674f, 15.556f, 10.738f,  // 550-554
            7.620f, 5.929f, 4.941f, 3.974f, 3.458f,  // 555-559
            0.130f, 2.536f, 2.433f, 2.085f, 2.366f,  // 560-564
            1.860f, 1.759f, 1.664f, 1.586f, 1.545f,  // 565-569
            1.570f, 1.998f, 2.178f, 2.664f, 3.424f,  // 570-574
            4.430f, 5.900f, 7.270f, 8.640f, 10.100f,  // 575-579
            12.180f, 11.976f, 12.672f, 13.368f, 14.064f,  // 580-584
            14.760f, 14.354f, 13.948f, 13.542f, 13.136f,  // 585-589
            14.230f, 12.132f, 11.534f, 10.936f, 10.338f,  // 590-594
            9.740f, 9.258f, 8.776f, 8.094f, 7.812f,  // 595-599
            1.530f, 7.808f, 5.286f, 8.764f, 8.242f,  // 600-604
            9.720f, 18.830f, 27.940f, 37.050f, 46.160f,  // 605-609
            64.270f, 53.732f, 51.194f, 47.656f, 45.118f,  // 610-614
            42.580f, 36.700f, 30.820f, 24.940f, 23.060f,  // 615-619
            9.000f, 12.460f, 12.280f, 12.444f, 12.786f,  // 620-624
            13.160f, 13.449f, 13.559f, 13.422f, 12.995f,  // 625-629
            8.760f, 11.225f, 9.922f, 8.409f, 6.769f,  // 630-634
            5.110f, 3.565f, 2.292f, 1.475f, 1.323f,  // 635-639
            5.070f, 2.124f, 2.178f, 2.232f, 2.286f,  // 640-644
            2.340f, 2.588f, 2.836f, 3.084f, 3.332f,  // 645-649
            3.580f, 3.466f, 3.352f, 3.238f, 3.124f,  // 650-654
            3.010f, 2.904f, 2.798f, 2.692f, 2.586f,  // 655-659
            2.480f, 2.412f, 2.344f, 2.276f, 2.208f,  // 660-664
            2.140f, 2.020f, 1.900f, 1.780f, 1.660f,  // 665-669
            1.540f, 1.498f, 1.456f, 1.414f, 1.372f,  // 670-674
            1.330f, 1.356f, 1.382f, 1.408f, 1.434f,  // 675-679
            1.460f, 1.556f, 1.652f, 1.748f, 1.844f,  // 680-684
            1.940f, 1.952f, 1.964f, 1.976f, 1.988f,  // 685-689
            2.000f, 1.840f, 1.680f, 1.520f, 1.360f,  // 690-694
            1.200f, 1.230f, 1.260f, 1.290f, 1.320f,  // 695-699
            1.350f, 1.900f, 2.450f, 3.000f, 3.550f,  // 700-704
            4.100f, 4.396f, 4.692f, 4.988f, 5.284f,  // 705-709
            5.580f, 4.966f, 4.352f, 3.738f, 3.124f,  // 710-714
            2.510f, 2.122f, 1.734f, 1.346f, 0.958f,  // 715-719
            0.570f, 0.510f, 0.450f, 0.390f, 0.330f,  // 720-724
            0.270f, 0.262f, 0.254f, 0.246f, 0.238f,  // 725-729
            0.230f, 0.226f, 0.222f, 0.218f, 0.214f,  // 730-734
            0.210f, 0.216f, 0.222f, 0.228f, 0.234f,  // 735-739
            0.240f, 0.240f, 0.240f, 0.240f, 0.240f,  // 740-744
            0.240f, 0.232f, 0.224f, 0.216f, 0.208f,  // 745-749
            0.200f, 0.208f, 0.216f, 0.224f, 0.232f,  // 750-754
            0.240f, 0.256f, 0.272f, 0.288f, 0.304f,  // 755-759
            0.320f, 0.308f, 0.296f, 0.284f, 0.272f,  // 760-764
            0.260f, 0.240f, 0.220f, 0.200f, 0.180f,  // 765-769
            0.160f, 0.152f, 0.144f, 0.136f, 0.128f,  // 770-774
            0.120f, 0.114f, 0.108f, 0.102f, 0.096f,  // 775-779
            0.090f                                       // 780-784
            // 785-789
            // 790-794
            // 795-799
            // 800-804
            // 805-809
            // 810-814
            // 815-819
            // 820-824
            // 825-829
            // 830
        };

    public TL84()
    {
        super( 380, 1, m_Readings );
    }
}