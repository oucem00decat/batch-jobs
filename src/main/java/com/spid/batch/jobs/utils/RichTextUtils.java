package com.spid.batch.jobs.utils;

import com.oxit.spid.transline.xmlsp.RichTextElement;
import lombok.experimental.UtilityClass;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.io.Serializable;

@UtilityClass
public final class RichTextUtils {

    public static String extractText(RichTextElement richTextElement) {
        final StringBuilder builder = new StringBuilder();

        for (Serializable contentItem : richTextElement.getContent()) {

            final String str;

            if (contentItem instanceof JAXBElement) {
                final JAXBElement<String> element = (JAXBElement<String>) contentItem;

                final QName tag = element.getName();

                final StringBuilder builder2 = new StringBuilder();

                builder2.append("<" + tag.getLocalPart() + ">");
                builder2.append(element.getValue());
                builder2.append("</" + tag.getLocalPart() + ">");
                str = builder2.toString();

            } else {
                str = (String) contentItem;
            }

            builder.append(str);
        }


        return builder.toString();
    }
}
