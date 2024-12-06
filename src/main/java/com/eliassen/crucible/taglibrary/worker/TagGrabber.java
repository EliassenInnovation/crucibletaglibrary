package com.eliassen.crucible.taglibrary.worker;

import com.eliassen.crucible.common.helpers.FileHelper;
import com.eliassen.crucible.common.helpers.Logger;
import com.eliassen.crucible.frameworkbrowser.helper.DataHelper;
import com.eliassen.crucible.frameworkbrowser.helper.FileGrabber;
import com.eliassen.crucible.frameworkbrowser.helper.GetResourceFilesRequest;
import com.eliassen.crucible.frameworkbrowser.helper.GetResourceFilesRequestBuilder;
import com.eliassen.crucible.frameworkbrowser.shared.Parser;
import com.eliassen.crucible.taglibrary.helper.TagFunctions;
import com.eliassen.crucible.taglibrary.helper.TagLibraryConstants;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagGrabber extends FileGrabber
{
    private List<String> tagPatternsToIgnore;

    public Set<String> getTagsFromTagDocumentation() throws ClassNotFoundException
    {
        Set<String> tagsInTagDocumentation = null;

        String tagDocumentationContent = new FileHelper()
                .getTextFileContent(
                TagFunctions.getTagLibrarySettingString(TagLibraryConstants.DOCUMENTATION_DIRECTORY)
                        + "/"
                        + TagFunctions.getTagLibrarySettingString(TagLibraryConstants.TAG_DOCUMENTATION_FILENAME));

        if(tagDocumentationContent != null && !tagDocumentationContent.isEmpty())
        {
            String parserName = TagFunctions.getTagLibrarySettingString(TagLibraryConstants.DOCUMENTATION_TAG_PARSER);
            tagsInTagDocumentation = getTagsFromFileContent(tagDocumentationContent, parserName);
        }

        return tagsInTagDocumentation;
    }

    public Set<String> getTagsFromFeatureFiles() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException
    {
        Map<String, Object> featureFiles = getFeatureFiles();
        return new TreeSet(getTagsFromFeatureFiles(featureFiles));
    }

    public Set<String> getTagsFromFeatureFiles(Map<String, Object> files) throws ClassNotFoundException
    {
        Set<String> tags = new HashSet<>();
        String parserName = TagFunctions.getTagLibrarySettingString(TagLibraryConstants.TAG_PARSER);

        for(Map.Entry<String, Object> record : files.entrySet())
        {
            if(record.getValue() instanceof Map)
            {
                tags.addAll(getTagsFromFeatureFiles((Map)record.getValue()));
            }
            else
            {
                tags.addAll(getTagsFromResource(record.getValue().toString(), parserName));
            }
        }

        return tags;
    }

    private Set<String> getTagsFromResource(String path, String parserName) throws ClassNotFoundException
    {
        String content = getFileContent(path);
        String filename = getPathLeaf(path);
        Set<String> tags = getTagsFromFileContent(content, filename, parserName);

        return tags;
    }

    private Set<String> getTagsFromFileContent(String content, String parserName) throws ClassNotFoundException
    {
        return getTagsFromFileContent(content, "",parserName);
    }

    private Set<String> getTagsFromFileContent(String content, String filename, String parserName) throws ClassNotFoundException
    {
        Set<String> tags = new HashSet<>();

        Parser tagParser = getParserByName(parserName);

        Pattern regex = Pattern.compile(tagParser.getExpression());
        Matcher regexMatcher = regex.matcher(content);
        boolean filenameProvided = filename != null && !filename.isEmpty();

        String match;
        while (regexMatcher.find())
        {
            match = regexMatcher.group(1);
            if(TagFunctions.getTagLibrarySettingBoolean(TagLibraryConstants.IGNORE_FEATURE_NAME_TAGS) &&
                    filenameProvided)
            {
                String filenameTag = "@" + filename.replace(".feature","");
                if(!filenameTag.equalsIgnoreCase(match))
                {
                    tags.add(match);
                }
            }
            else
            {
                tags.add(match);
            }
        }

        checkTagsSetForTagsToIgnore(tags);

        return tags;
    }

    private void checkTagsSetForTagsToIgnore(Set<String> tags)
    {
        List<String> tagsToRemove = new ArrayList<>();
        if(getTagPatternsToIgnore() != null && !getTagPatternsToIgnore().isEmpty())
        {
            for (String tag : tags)
            {
                for (String tagPatternToIgnore : getTagPatternsToIgnore())
                {
                    if(tag.toLowerCase(Locale.ROOT).contains(tagPatternToIgnore.toLowerCase(Locale.ROOT)))
                    {
                        tagsToRemove.add(tag);
                    }
                }
            }
        }

        for(String tagToRemove : tagsToRemove)
        {
            tags.remove(tagToRemove);
        }
    }

    public Map<String, Object> getFeatureFiles() throws ClassNotFoundException, InvocationTargetException,
            NoSuchMethodException, InstantiationException, IllegalAccessException
    {
        GetResourceFilesRequest request = new GetResourceFilesRequestBuilder()
                .setUseFeatureNames(TagFunctions.getTagLibrarySettingBoolean(TagLibraryConstants.USE_FEATURE_NAMES))
                .setUseExternalJar(TagFunctions.getTagLibrarySettingBoolean(TagLibraryConstants.USE_EXTERNAL_JAR))
                .setBaseName(TagFunctions.getTagLibrarySettingString(TagLibraryConstants.JAR_PATH))
                .setSource(TagFunctions.getTagLibrarySettingString(TagLibraryConstants.SOURCE))
                .setExcludeFileTypes(TagFunctions.getTagLibrarySettingArray(TagLibraryConstants.EXCLUDE_FILE_TYPES))
                .setBasePath(TagFunctions.getTagLibrarySettingString(TagLibraryConstants.BASE_PATH))
                .build();

        this.request = request;

        return getResourceFiles(request);
    }

    public List<String> getTagPatternsToIgnore()
    {
        if(tagPatternsToIgnore == null)
        {
            JSONArray tagsToIgnoreJson = TagFunctions.getTagLibrarySettingArray(TagLibraryConstants.TAG_PATTERNS_TO_IGNORE);
            tagPatternsToIgnore = DataHelper.convertJsonArrayToStringList(tagsToIgnoreJson);
        }
        return tagPatternsToIgnore;
    }

    public Set<String> getTagsMissingDocumentation() throws IOException, ClassNotFoundException,
            InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException
    {
        Set<String> missingTags = null;
        Set<String> tagsInDocumentation = null;
        Set<String> tagsInFramework = null;

        String documentationFileName = TagFunctions.getTagLibrarySettingString(TagLibraryConstants.DOCUMENTATION_DIRECTORY)
                + "/"
                + TagFunctions.getTagLibrarySettingString(TagLibraryConstants.TAG_DOCUMENTATION_FILENAME);

        //check if tag documentation file exists
        File documentation = new File(documentationFileName);

        if(!documentation.exists())
        {
            Logger.logError("file does not exist: " + documentationFileName + " creating");
            new TagMarkdownWriter().createFileFromTemplate(documentation,
                    TagFunctions.getTagLibrarySettingString(TagLibraryConstants.TAG_DOCUMENTATION_FILENAME),
                    "tagsMarkdownTemplates.json");
        }
        else
        {
            tagsInDocumentation = getTagsFromTagDocumentation();
        }

        //refresh tagList
        tagsInFramework = generateTagList();

        if(tagsInDocumentation != null && tagsInDocumentation.size() > 0)
        {
            missingTags = TagFunctions.getTagsMissingFromControlSet(tagsInFramework, tagsInDocumentation);
        }
        else
        {
            missingTags = tagsInFramework;
        }

        return missingTags;
    }

    public Set<String> generateTagList() throws IOException, ClassNotFoundException, InvocationTargetException,
            NoSuchMethodException, InstantiationException, IllegalAccessException
    {
        Set<String> tags = getTagsFromFeatureFiles();
        new TagMarkdownWriter().createTagListMarkdownFile(
                TagFunctions.getTagLibrarySettingString(TagLibraryConstants.DOCUMENTATION_DIRECTORY) +"/" +
                        TagFunctions.getTagLibrarySettingString(TagLibraryConstants.TAG_LIST_FILENAME),tags);

        return tags;
    }

    public Set<String> generateTagsMissingDocumentation() throws IOException, ClassNotFoundException,
            InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException
    {
        Set<String> tagsMissingDocumentation = getTagsMissingDocumentation();
        new TagMarkdownWriter().createTagsMissingDocumentationMarkdownFile(
                TagFunctions.getTagLibrarySettingString(TagLibraryConstants.DOCUMENTATION_DIRECTORY) +"/" +
                        TagFunctions.getTagLibrarySettingString(TagLibraryConstants.TAGS_MISSING_DOCUMENTATION_FILENAME)
                        ,tagsMissingDocumentation);

        return tagsMissingDocumentation;
    }

    @Override
    public String getParserFileName()
    {
        return TagFunctions.getTagLibrarySettingString(TagLibraryConstants.PARSER_JSON);
    }
}
