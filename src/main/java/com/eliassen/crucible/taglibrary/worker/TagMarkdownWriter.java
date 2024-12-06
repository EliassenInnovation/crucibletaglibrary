package com.eliassen.crucible.taglibrary.worker;

import com.eliassen.crucible.frameworkbrowser.worker.MarkdownCreator;
import net.steppschuh.markdowngenerator.text.Text;
import net.steppschuh.markdowngenerator.text.heading.Heading;

import java.io.IOException;
import java.util.Set;

public class TagMarkdownWriter extends MarkdownCreator
{
    public void createTagListMarkdownFile(String fileNamePath, Set<String> tags) throws IOException
    {
        tagMarkdownCreationCommon(fileNamePath, "Tag List", tags);
    }

    public void createTagsMissingDocumentationMarkdownFile(String fileNamePath, Set<String> tags) throws IOException
    {
        tagMarkdownCreationCommon(fileNamePath, "Tags Missing Documentation", tags);
    }

    private void tagMarkdownCreationCommon(String fileNamePath, String headerText, Set<String> tags) throws IOException
    {
        StringBuilder tagListContent = createTagListMarkDownContent(tags, headerText);
        markdownCreationCommon(fileNamePath, tagListContent.toString());
    }

    public StringBuilder createTagListMarkDownContent(Set<String> tags, String headerText)
    {
        StringBuilder tagListContent = new StringBuilder();
        tagListContent.append(new Heading(headerText,1)).append("\n");
        for(String tag : tags)
        {
            tagListContent.append(new Text(tag)).append("  \n");
        }

        return tagListContent;
    }
}
