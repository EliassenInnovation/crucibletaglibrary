package com.eliassen.taglibrary.unittests;

import com.eliassen.crucible.taglibrary.worker.TagGrabber;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class FileGrabbingTests
{
    private TagGrabber tagGrabber;

    @BeforeEach
    public void init()
    {
        tagGrabber = new TagGrabber();
    }

    @Test
    public void canGrabFeatureFiles()
    {
        try
        {
            Map<String, Object> featureFIles = tagGrabber.getFeatureFiles();
            assertTrue(featureFIles.size() > 0);
        }
        catch(Exception e)
        {
            fail("Exception was thrown: " + e.getMessage());
        }
    }

    @Test
    public void canFindTags()
    {
        try
        {
            Set<String> tags = tagGrabber.getTagsFromFeatureFiles();
            assertTrue(tags.size()>0);
        }
        catch(Exception e)
        {
            fail("Exception was thrown: " + e.getMessage());
        }
    }

    @AfterEach
    public void cleanUp()
    {
        tagGrabber = null;
    }
}
