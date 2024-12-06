package com.eliassen.crucible.taglibrary;

import com.eliassen.crucible.common.helpers.FileHelper;
import com.eliassen.crucible.taglibrary.helper.TagFunctions;
import com.eliassen.crucible.taglibrary.helper.TagLibraryConstants;
import com.eliassen.crucible.taglibrary.worker.TagGrabber;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public class Main
{
    public static void main(String[] args) throws ClassNotFoundException, InvocationTargetException,
            NoSuchMethodException, InstantiationException, IllegalAccessException, IOException
    {
        TagGrabber tagGrabber = new TagGrabber();
        FileHelper.ensureDirectoryExists(TagFunctions.getTagLibrarySettingString(TagLibraryConstants.DOCUMENTATION_DIRECTORY));
        Set<String> tagsMissingDocumentation = tagGrabber.generateTagsMissingDocumentation();
    }
}