package cn.com.shopgroup.user.service;

import cn.com.shopgroup.user.model.GbImageLibraryInfo;

import java.util.List;

public interface GbImageLibraryInfoService {


    List<GbImageLibraryInfo> getImageList(Long leaderId, int page, int pageSize);


    Long getImageCount(Long leaderId);

    GbImageLibraryInfo getImageInfo(Long leaderId, Long imgId);

    Boolean addImage(Long leaderId, Byte imgType, String imgUrl, Byte isPrivate);

    Boolean removeImage(Long leaderId, Long imgId);
}
