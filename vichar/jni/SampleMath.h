/*==============================================================================
            Copyright (c) 2012 QUALCOMM Austria Research Center GmbH.
            All Rights Reserved.
            Qualcomm Confidential and Proprietary
            
@file 
    SampleMath.h

@brief
    A utility class.

==============================================================================*/


#ifndef _QCAR_SAMPLEMATH_H_
#define _QCAR_SAMPLEMATH_H_

// Includes:
#include <QCAR/Tool.h>

/// A utility class used by the QCAR SDK samples.
class SampleMath
{
public:
    
    static QCAR::Vec2F Vec2FSub(QCAR::Vec2F v1, QCAR::Vec2F v2);
    
    static float Vec2FDist(QCAR::Vec2F v1, QCAR::Vec2F v2);
    
    static QCAR::Vec3F Vec3FAdd(QCAR::Vec3F v1, QCAR::Vec3F v2);
    
    static QCAR::Vec3F Vec3FSub(QCAR::Vec3F v1, QCAR::Vec3F v2);
    
    static QCAR::Vec3F Vec3FScale(QCAR::Vec3F v, float s);
    
    static float Vec3FDot(QCAR::Vec3F v1, QCAR::Vec3F v2);
    
    static QCAR::Vec3F Vec3FCross(QCAR::Vec3F v1, QCAR::Vec3F v2);
    
    static QCAR::Vec3F Vec3FNormalize(QCAR::Vec3F v);
    
    static QCAR::Vec3F Vec3FTransform(QCAR::Vec3F& v, QCAR::Matrix44F& m);
    
    static QCAR::Vec3F Vec3FTransformNormal(QCAR::Vec3F& v, QCAR::Matrix44F& m);
    
    static QCAR::Vec4F Vec4FTransform(QCAR::Vec4F& v, QCAR::Matrix44F& m);
    
    static QCAR::Vec4F Vec4FDiv(QCAR::Vec4F v1, float s);

    static QCAR::Matrix44F Matrix44FIdentity();
    
    static QCAR::Matrix44F Matrix44FTranspose(QCAR::Matrix44F m);
    
    static float Matrix44FDeterminate(QCAR::Matrix44F& m);
    
    static QCAR::Matrix44F Matrix44FInverse(QCAR::Matrix44F& m);

  //Begin additions by Erin===========================================================================================
    /**
     * Calculates [R^-1|-R^-1*T] from [R|T]
     * param m posMatrix in form [R|T]
     * return posMatrix in form [R^-1|-R^-1*T]
     */
    static QCAR::Matrix34F phoneCoorMatrix(QCAR::Matrix34F m);

    /**
     * Calculates -R^-1*T
     * param m posMatrix in form [R^-1|T]
     */
    static void matrxVecMult(QCAR::Matrix34F *m);

    /**
     * Calculates [R1^-1|T2] given [R1^-1|T1] and [R2|T2]
     * param m posMatrix [R1^-1|T1]
     * param n pointer to posMatrix [R2|T2]
     */
    static void swapRotPos(QCAR::Matrix34F m, QCAR::Matrix34F *n);
//End==========================================================================================================
    
};

#endif // _QCAR_SAMPLEMATH_H_