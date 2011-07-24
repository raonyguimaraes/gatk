/*
 * Copyright (c) 2011, The Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package org.broadinstitute.sting.utils.help;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.Tag;
import org.broadinstitute.sting.commandline.*;
import org.broadinstitute.sting.gatk.CommandLineGATK;
import org.broadinstitute.sting.utils.Utils;
import org.broadinstitute.sting.utils.classloader.JVMUtils;
import org.broadinstitute.sting.utils.exceptions.ReviewedStingException;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 *
 */
public class GenericDocumentationHandler extends DocumentedGATKFeatureHandler {
    @Override
    public boolean shouldBeProcessed(ClassDoc doc) {
        try {
            Class type = HelpUtils.getClassForDoc(doc);
            return JVMUtils.isConcrete(type);
        } catch ( ClassNotFoundException e ) {
            return false;
        }
    }


    @Override
    public String getTemplateName(ClassDoc doc) throws IOException {
        return "generic.template.html";
    }

    @Override
    public void processOne(GATKDoclet.DocWorkUnit toProcess, Map<Class, GATKDoclet.DocWorkUnit> all) {
        System.out.printf("%s class %s%n", toProcess.group, toProcess.classDoc);
        ClassDoc classdoc = toProcess.classDoc;
        Map<String, Object> root = new HashMap<String, Object>();

        root.put("name", classdoc.name());

        // Extract overrides from the doc tags.
        StringBuilder summaryBuilder = new StringBuilder();
        for(Tag tag: classdoc.firstSentenceTags())
             summaryBuilder.append(tag.text());
        root.put("summary", summaryBuilder.toString());
        root.put("description", classdoc.commentText());

        for(Tag tag: classdoc.tags()) {
            root.put(tag.name(), tag.text());
        }

        ParsingEngine parsingEngine = createStandardGATKParsingEngine();

        Map<String, List<Object>> args = new HashMap<String, List<Object>>();
        root.put("arguments", args);
        args.put("all", new ArrayList<Object>());
        args.put("required", new ArrayList<Object>());
        args.put("optional", new ArrayList<Object>());
        args.put("hidden", new ArrayList<Object>());
        args.put("depreciated", new ArrayList<Object>());
        try {
            for ( ArgumentSource argumentSource : parsingEngine.extractArgumentSources(HelpUtils.getClassForDoc(classdoc)) ) {
                ArgumentDefinition argDef = argumentSource.createArgumentDefinitions().get(0);
                FieldDoc fieldDoc = getFieldDoc(classdoc, argumentSource.field.getName());
                GATKDoc doc = docForArgument(fieldDoc, argDef); // todo -- why can you have multiple ones?
                String kind = "optional";
                if ( argumentSource.isRequired() ) kind = "required";
                else if ( argumentSource.isHidden() ) kind = "hidden";
                else if ( argumentSource.isDeprecated() ) kind = "depreciated";
                args.get(kind).add(doc.toDataModel());
                args.get("all").add(doc.toDataModel());
                System.out.printf("Processing %s%n", argumentSource);
            }
        } catch ( ClassNotFoundException e ) {
            throw new RuntimeException(e);
        }

        List<Map<String, Object>> extraDocsData = new ArrayList<Map<String, Object>>();
        for ( Class extraDocClass : toProcess.annotation.extraDocs() ) {
            final GATKDoclet.DocWorkUnit otherUnit = all.get(extraDocClass);
            if ( otherUnit == null )
                throw new ReviewedStingException("Requested extraDocs for class without any documentation: " + extraDocClass);
            extraDocsData.add(
                    new HashMap<String, Object>(){{
                        put("filename", otherUnit.filename);
                        put("name", otherUnit.name);}});

        }
        root.put("extradocs", extraDocsData);

        //System.out.printf("Root is %s%n", root);
        toProcess.setHandlerContent(summaryBuilder.toString(), root);
    }

    protected ParsingEngine createStandardGATKParsingEngine() {
        CommandLineProgram clp = new CommandLineGATK();
        try {
            CommandLineProgram.start(clp, new String[]{}, true);
            return clp.parser;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private FieldDoc getFieldDoc(ClassDoc classDoc, String name) {
        return getFieldDoc(classDoc, name, true);
    }

    private FieldDoc getFieldDoc(ClassDoc classDoc, String name, boolean primary) {
        //System.out.printf("Looking for %s in %s%n", name, classDoc.name());
        for ( FieldDoc fieldDoc : classDoc.fields(false) ) {
            //System.out.printf("fieldDoc " + fieldDoc + " name " + fieldDoc.name());
            if ( fieldDoc.name().equals(name) )
                return fieldDoc;

            Field field = HelpUtils.getFieldForFieldDoc(fieldDoc);
            if ( field.isAnnotationPresent(ArgumentCollection.class) ) {
                ClassDoc typeDoc = getRootDoc().classNamed(fieldDoc.type().qualifiedTypeName());
                if ( typeDoc == null )
                    throw new ReviewedStingException("Tried to get javadocs for ArgumentCollection field " + fieldDoc + " but could't find the class in the RootDoc");
                else {
                    FieldDoc result = getFieldDoc(typeDoc, name, false);
                    if ( result != null )
                        return result;
                    // else keep searching
                }
            }
        }

        // if we didn't find it here, wander up to the superclass to find the field
        if ( classDoc.superclass() != null ) {
            return getFieldDoc(classDoc.superclass(), name, false);
        }

        if ( primary )
            throw new RuntimeException("No field found for expected field " + name);
        else
            return null;
    }

    protected GATKDoc docForArgument(FieldDoc fieldDoc, ArgumentDefinition def) {
        final String name = def.fullName != null ? "--" + def.fullName : "-" + def.shortName;
        GATKDoc doc = new GATKDoc(GATKDoc.DocType.WALKER_ARG, name);

        if ( def.fullName != null && def.shortName != null)
            doc.addSynonym("-" + def.shortName);

        doc.addTag("required", def.required ? "yes" : "no");
        doc.addTag("type", def.argumentType.getSimpleName());
        if ( def.doc != null ) doc.setSummary(def.doc);

        List<String> attributes = new ArrayList<String>();
        attributes.add(def.ioType.annotationClass.getSimpleName());
        if ( def.required ) attributes.add("required");
        if ( def.isFlag ) attributes.add("flag");
        if ( def.isHidden ) attributes.add("hidden");
        doc.addTag("attributes", Utils.join(",", attributes));

        // todo -- need depreciated value

        doc.addTag("options", def.validOptions == null ? GATKDoc.NA_STRING : Utils.join(",", def.validOptions));

        doc.setFulltext(fieldDoc.commentText());

        return doc;
    }
}
