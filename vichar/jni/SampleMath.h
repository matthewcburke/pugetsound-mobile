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
      static QCAR::Matrix34F phoneCoorMatrix(QCAR::Matrix34F *m);

      /**
       * Helper method: Calculates -R^-1*T
       * param m posMatrix in form [R^-1|T]
       */
      static void matrxVecMult(QCAR::Matrix34F *m);

      /**
       * Calculates [R1^-1|-R1^-1*T2] given [R1^-1|T1] and [R2|T2]
       * param m pointer to posMatrix [R1|T1]
       * param n posMatrix [R2|T2]
       * return pos matrix in form [R1^-1|-R1^-1*T2]
       */
      static QCAR::Matrix34F calcSecondPos(QCAR::Matrix34F *m, QCAR::Matrix34F *n);

      /**
       * Gets the relative position between two objects with pos matrices & [R1^-1|T2]
       * param m pointer to object 1, must be in form [R^-1|-R^-1*T]
       * param n pointer to object 2, must be in form [R2|T2]
       * return posMatrix with position of object two with rotation to object 1
       */
      static QCAR::Matrix34F vectorAdd(QCAR::Matrix34F *m, QCAR::Matrix34F *n);

      /**
       * Calculates the distance between the phone and a target
       * param phone pointer to the phones pos matrix
       * return The distance between the phone and a target
       */
      static float getDistance(QCAR::Matrix34F *phone);

      /**
      * Calculates three Euler angles (theta_x, theta_y, theta_z) from a 3x3 rotation
      * matrix nested inside a posMatrix of the form [R^-1|-R^-1*T]
      * param m pointer to posMatrix in form [R^-1|-R^-1*T]
      * return pointer to a float array containing the three Euler angles (theta_x, theta_y, theta_z)
      */
      static QCAR::Vec3F getEulerAngles(QCAR::Matrix34F *m);
    //End===============================================================================================================
    
};

#endif // _QCAR_SAMPLEMATH_H_
