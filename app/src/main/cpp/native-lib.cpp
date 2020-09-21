#include <jni.h>
#include <string>
#include <iostream>
#include <dlib/matrix.h>
#include <sstream>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc.hpp>
#include "dlib_proc.h"
#include "skin_makeup.h"
#include "lip_makeup.h"
#include "eyeshadow.h"
#include "eyebrow.h"
#include "eyeliner.h"
#include "eyelash.h"
#include "blush.h"
#include <android/log.h>
#include "jni_bridge.h"
#include <array>

using namespace dlib;
using namespace std;
using namespace cv ;
extern "C" JNIEXPORT jintArray


JNICALL
Java_com_example_ai_dlibdemo_MainActivity_Bitmap2proc(JNIEnv *env, jobject /* this */,
                                                      jintArray buf, jintArray eye_left,
                                                      jintArray eye_right,
                                                      jintArray p_eyebrow_left,
                                                      jintArray p_eyebrow_right,
                                                      jintArray blush_l,
                                                      jintArray blush_r,
                                                      jobject lash,
                                                      jintArray w, jintArray h,
                                                      jintArray points, jint type,
                                                      jint left, jint top, jint right, jint bootom) {
    //声明jc+指针
    jint *cbuf, *cw, *ch, *ceye_left, *ceye_right, *cleftb, *crightb, *cblush_l, *cblush_r;
    jboolean ptfalse = false;


    AndroidBitmapInfo mask_info;
    uint32_t *mask_pixels = lockJavaBitmap(env, lash, mask_info);
    assert(mask_pixels != nullptr && mask_info.format == ANDROID_BITMAP_FORMAT_A_8);
    Mat mask(mask_info.height, mask_info.width, CV_8UC1, mask_pixels);


    cbuf = env->GetIntArrayElements(buf, &ptfalse);
    cw = env->GetIntArrayElements(w, &ptfalse);
    ch = env->GetIntArrayElements(h, &ptfalse);
    ceye_left = env->GetIntArrayElements(eye_left, &ptfalse);
    ceye_right = env->GetIntArrayElements(eye_right, &ptfalse);
    cleftb = env->GetIntArrayElements(p_eyebrow_left, &ptfalse);
    crightb = env->GetIntArrayElements(p_eyebrow_right, &ptfalse);
    cblush_l = env->GetIntArrayElements(blush_l, &ptfalse);
    cblush_r = env->GetIntArrayElements(blush_r, &ptfalse);

    jsize len = env->GetArrayLength(points);
    if (cbuf == NULL || len <= 0) {
        return 0;
    }


    Mat imgData(ch[0], cw[0], CV_8UC4, (unsigned char *) cbuf);
    Mat ret_imgData(ch[0], cw[0], CV_8UC4);
    Mat leyeData(ch[1], cw[1], CV_8UC4, (unsigned char *) ceye_left);
    Mat reyeData(ch[2], cw[2], CV_8UC4, (unsigned char *) ceye_right);
    Mat lbrowData(ch[3], cw[3], CV_8UC4, (unsigned char *) cleftb);
    Mat rbrowData(ch[4], cw[4], CV_8UC4, (unsigned char *) crightb);
    Mat lblushData(ch[5], cw[5], CV_8UC4, (unsigned char *) cblush_l);
    Mat rblushData(ch[6], cw[6], CV_8UC4, (unsigned char *) cblush_r);

    //Mat half_imgData;
    // resize(imgData, half_imgData,Size(),0.3,0.3);
    cvtColor(imgData, imgData, CV_BGRA2BGR);
    cvtColor(leyeData, leyeData, CV_BGRA2BGR);
    cvtColor(reyeData, reyeData, CV_BGRA2BGR);
    cvtColor(lbrowData, lbrowData, CV_BGRA2BGR);
    cvtColor(rbrowData, rbrowData, CV_BGRA2BGR);

    jintArray array = env->NewIntArray(len);
    jint *body = env->GetIntArrayElements(points, 0);

    std::vector<Point> shape_points;
    std::vector<Point> face_points;
    for (int i = 0; i < len / 2; i++) {
        shape_points.push_back(Point(body[2 * i], body[2 * i + 1]));
        face_points.push_back(Point(body[2 * i] - left, body[2 * i + 1] - top));
    }
    Mat facedata = imgData(Rect(left, top, right - left, bootom - top));
    switch (type) {
        case 1:
            //嘴唇化妆
            makeLip(imgData, shape_points);

            //眼影
            eyeshadow(imgData, shape_points, leyeData, reyeData);

            //眉毛
            eyebrow(imgData, shape_points, lbrowData, rbrowData);

            //眼线
            eyeliner_makeup(imgData, shape_points);

            //睫毛
            cvtColor(imgData, imgData, CV_BGR2BGRA);
            eyelash(imgData, mask, shape_points);
            cvtColor(imgData, imgData, CV_BGRA2BGR);

            //腮红
            blush(imgData, shape_points, lblushData, rblushData);
            //磨皮
            smooth_skin(imgData);

            break;
        case 2:

            //嘴唇化妆
            makeLip(imgData, shape_points);
            break;
        case 3:
            //眼影
            eyeshadow(imgData, shape_points, leyeData, reyeData);
            break;
        case 4:
            //眉毛
            eyebrow(imgData, shape_points, lbrowData, rbrowData);
            break;
        case 5:
            //磨皮
            smooth_skin(imgData);
            break;

        case 6:
            //睫毛
            cvtColor(imgData, imgData, CV_BGR2BGRA);
            eyelash(imgData, mask, shape_points);
            cvtColor(imgData, imgData, CV_BGRA2BGR);
            break;

        case 7:
            //腮红
            blush(imgData, shape_points, lblushData, rblushData);
            break;

        case 8:
            //眼线
            eyeliner_makeup(imgData, shape_points);

            break;
        case 9:
            //瘦脸
            

            break;
        default:
            break;
    }
//    switch (type) {
//        case 1:
//            //嘴唇化妆
//            makeLip(facedata, face_points);
//
//            //眼影
//            eyeshadow(facedata, face_points, leyeData, reyeData);
//
//            //眉毛
//            eyebrow(facedata, face_points);
//
//            //磨皮
//            smooth_skin(facedata);
//            break;
//        case 2:
//
//            //嘴唇化妆
//            makeLip(facedata, face_points);
//            break;
//        case 3:
//            //眼影
//            eyeshadow(facedata, face_points, leyeData, reyeData);
//            break;
//        case 4:
//            //眉毛
//            eyebrow(facedata, face_points);
//            break;
//        case 5:
//            //磨皮
//            smooth_skin(facedata);
//            break;
//        default:
//            break;
//    }

    resize(imgData, imgData, Size(cw[0], ch[0]));
    cvtColor(imgData, ret_imgData, CV_BGR2BGRA);
    int size = cw[0] * ch[0];
    jintArray result = env->NewIntArray(size);
    env->SetIntArrayRegion(result, 0, size, (jint *) ret_imgData.data);
    env->ReleaseIntArrayElements(buf, cbuf, 0);
    unlockJavaBitmap(env, lash);
    return result;
}


//extern "C"
//JNIEXPORT void JNICALL
//Java_com_example_ai_dlibdemo_TestActivcity_Makeup(
//        JNIEnv *env, jobject instance, jobject bitmap1, jobject bitmap2, jobject lash) {


//    AndroidBitmapInfo dst_info;
//    uint32_t *dst_pixels = lockJavaBitmap(env, bitmap1, dst_info);
//    Mat dst(dst_info.height, dst_info.width, CV_8UC4, dst_pixels);
//
//    AndroidBitmapInfo src_info;
//    uint32_t *src_pixels = lockJavaBitmap(env, bitmap2, src_info);
//    Mat src(src_info.height, src_info.width, CV_8UC4, src_pixels);
//
//    AndroidBitmapInfo mask_info;
//    uint32_t *mask_pixels = lockJavaBitmap(env, lash, mask_info);
//    assert(mask_pixels != nullptr && mask_info.format == ANDROID_BITMAP_FORMAT_A_8);
//    Mat mask(mask_info.height, mask_info.width, CV_8UC1, mask_pixels);
//
//    std::vector<Point> shape_points;
//    eyelash(dst, mask, shape_points);
//    // applyEyeLash(dst, src, points, mask, color, amount);
//
//    unlockJavaBitmap(env, bitmap1);
//    unlockJavaBitmap(env, bitmap2);
//    unlockJavaBitmap(env, lash);
//}

void loadKeypoints(JNIEnv *env,jintArray points, std::vector<cv::Point2f>& keypoints)
{
    jsize len = env->GetArrayLength(points);
    jint *body = env->GetIntArrayElements(points, 0);
    for (int i = 0; i < len/2; i++)
    {
        keypoints.push_back(Point(body[2 * i], body[2 * i + 1]));
    }
}

void drawTriangles(Mat img, const std::vector<std::array<cv::Point2f, 3>>& triangles, Scalar& color)
{
    std::vector<cv::Point2i> p;
    for (auto& i : triangles)
    {
        for (auto& j : i)
        {
            p.push_back(cv::Point2i{ (int)j.x,(int)j.y });
        }

        //绘制三角形
        polylines(img, p, true, color);
        p.clear();

    }

}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_ai_dlibdemo_FaceChangeActivity_faceExchange(JNIEnv *env, jobject instance,
                                                             jobject source1, jobject source2,
                                                             jintArray points1_,
                                                             jintArray points2_) {
    jint *points1 = env->GetIntArrayElements(points1_, NULL);
    jint *points2 = env->GetIntArrayElements(points2_, NULL);

    //----------------------bitmap转换成mat------------------------------
    Mat *img1 = getNativeMat(env, source1);
    Mat srcImg1 = img1->clone();
    Mat *img2 = getNativeMat(env, source2);
    Mat srcImg2 = img2->clone();

    std::vector<Point2f> landmark1;
    std::vector<Point2f> landmark2;
    loadKeypoints(env,points1_,landmark1);
    loadKeypoints(env,points1_,landmark2);
    //----------------------三角剖分------------------------------
    //只需要对图1进行三角剖分即可
    Subdiv2D subdiv;
    subdiv.initDelaunay(Rect{ 0,0,srcImg1.cols,srcImg1.rows });
    subdiv.insert(landmark1);
    std::vector<Vec6f> triangleList1;
    std::vector<std::array<int, 3>> correspondIdx1;
    std::vector<std::array<Point2f, 3>> triangles1;
    std::vector<std::array<Point2f, 3>> triangles2;
    subdiv.getTriangleList(triangleList1);

    Rect roi_rect = Rect(0, 0, srcImg1.cols,srcImg1.rows);
    int tempIdx[3] = { 0 };
    for (auto& i : triangleList1)
    {
        //获取三角行的顶点
        Point2f p[3];
        p[0] = Point2f{ i[0],i[1] };
        p[1] = Point2f{ i[2],i[3] };
        p[2] = Point2f{ i[4],i[5] };

        //判断顶点是否在ROI矩形内
        if (roi_rect.contains(p[0]) && roi_rect.contains(p[1]) && roi_rect.contains(p[2]))
        {
            triangles1.push_back({ p[0],p[1],p[2] });
            //确定三角形的顶点在人脸关键点中的索引下标
            for (int j = 0; j < 3; j++)
            {
                for (int k = 0; k < landmark1.size(); k++)
                {
                    if ((p[j].x == landmark1[k].x) && (p[j].y == landmark1[k].y))
                    {
                        tempIdx[j] = k;
                    }
                }

            }
            correspondIdx1.push_back(std::array<int, 3>{tempIdx[0], tempIdx[1], tempIdx[2]});
        }


    }

    //--------------------------仿射变换-----------------------------------
    //根据图像1的索引结果，得到图像2的三角剖分
    for (auto& i : correspondIdx1)
    {
        Point2f p[3];
        p[0] = Point2f{ landmark2[i[0]].x,landmark2[i[0]].y };
        p[1] = Point2f{ landmark2[i[1]].x,landmark2[i[1]].y };
        p[2] = Point2f{ landmark2[i[2]].x,landmark2[i[2]].y };

        triangles2.push_back({ p[0],p[1],p[2] });
    }

    Mat _srcImg2 = srcImg2.clone();  //图像2进行复制，目的是保留原始图像
    //drawTriangles(_srcImg2, triangles2, Scalar(0, 255, 255));
    //drawTriangles(srcImg1, triangles1, Scalar(0, 255, 255));

    //仿射变换
    for (int i = 0; i < triangles1.size(); i++)
    {
        //确定ROI
        Rect roi_1 = boundingRect(std::vector<cv::Point2f>{triangles1[i].begin(), triangles1[i].end()});
        Rect roi_2 = boundingRect(std::vector<cv::Point2f>{triangles2[i].begin(), triangles2[i].end()});
        //ROI区域的图像,图像1
        Mat roi_img = srcImg1(roi_1);

        //减去ROI左上角坐标
        std::vector<cv::Point2f> triangle1_nor;
        std::vector<cv::Point2f> triangle2_nor;
        for (auto& j : triangles1[i])
        {
            //Point2f与Point2i不能直接相加减，必须进行类型转换
            triangle1_nor.push_back(j - Point2f{ (float)roi_1.tl().x,(float)roi_1.tl().y });
        }
        for (auto& k : triangles2[i])
        {
            triangle2_nor.push_back(k - Point2f{ (float)roi_2.tl().x,(float)roi_2.tl().y });
        }


        //计算图像1的每个三角形到图像2对应的三角形的仿射变换矩阵
        Mat M;
        M = getAffineTransform(triangle1_nor, triangle2_nor);
        Mat imgWarp;
        //仿射变换
        warpAffine(roi_img, imgWarp, M, roi_2.size(), 1, BorderTypes::BORDER_REFLECT_101);
        //制作图像2的局部mask
        Mat mask = Mat::zeros(roi_2.size(), CV_8UC1);
        //fill绘图函数输入坐标必须为int类型
        std::vector<cv::Point2i> triangle2_nor_int;
        for (auto& n : triangle2_nor)
        {
            triangle2_nor_int.push_back(Point2i{ (int)n.x,(int)n.y });
        }
        fillConvexPoly(mask, triangle2_nor_int, Scalar(255));
        //带mask复制
        imgWarp.copyTo(_srcImg2(roi_2), mask);
        /*imshow("temp", _srcImg2);
        waitKey(0);
            */
    }

    //---------------------图像融合-------------------------------------
    //因为经过仿射变换之后的人脸只是形状上吻合，但是边缘太生硬，需要进行图像融合
    //凸包所在的点，组成的集合其实是人脸边界轮廓
    std::vector<cv::Point2i> convexPoints2;
    std::vector<int> convexPointsIdx2;
    convexHull(landmark2, convexPointsIdx2);
    for (auto& i : convexPointsIdx2)
    {
        convexPoints2.push_back(landmark2[i]);
    }
    //制作mask
    Mat faceMask = Mat::zeros(srcImg2.size(), CV_8UC1);
    fillConvexPoly(faceMask, convexPoints2, Scalar(255));
    //采用seamlessClone进行图像融合，效果较好，Microsoft NB的算法,无缝融合功能
    Mat resultImg;
    Point2i position = (boundingRect(convexPoints2).tl() + boundingRect(convexPoints2).br()) / 2;
    seamlessClone(_srcImg2, srcImg2, faceMask, position, resultImg, NORMAL_CLONE);

    //绘制三角剖分
    Mat triangleImg[2];
    triangleImg[0] = srcImg1.clone();
    triangleImg[1] = srcImg2.clone();
    Scalar color = Scalar(0, 255, 255);
    drawTriangles(triangleImg[1], triangles2, color);
    drawTriangles(triangleImg[0], triangles1, color);

    env->ReleaseIntArrayElements(points1_, points1, 0);
    env->ReleaseIntArrayElements(points2_, points2, 0);

    return getJavaMat(env,resultImg);
}