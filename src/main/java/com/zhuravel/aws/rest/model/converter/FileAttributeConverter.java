package com.zhuravel.aws.rest.model.converter;

import com.zhuravel.aws.rest.model.FileAttribute;
import com.zhuravel.aws.rest.model.FileAttributeItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Evgenii Zhuravel created on 27.10.2022
 */
@Component
public class FileAttributeConverter {

    public ArrayList<FileAttributeItem> convert(Iterator<FileAttribute> works) {
        FileAttributeItem fileAttributeItem;
        ArrayList<FileAttributeItem> itemList = new ArrayList<>();

        while (works.hasNext()) {
            fileAttributeItem = new FileAttributeItem();
            FileAttribute fileAttribute = works.next();
            fileAttributeItem.setId(fileAttribute.getId());
            fileAttributeItem.setDate(fileAttribute.getDate());
            fileAttributeItem.setFilename(fileAttribute.getFilename());
            fileAttributeItem.setSize(fileAttribute.getSize());
            fileAttributeItem.setUrl(fileAttribute.getUrl());

            itemList.add(fileAttributeItem);
        }
        return itemList;
    }
}
