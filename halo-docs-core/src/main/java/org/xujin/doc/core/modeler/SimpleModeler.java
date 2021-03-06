package org.xujin.doc.core.modeler;

import org.xujin.doc.core.Category;
import org.xujin.doc.core.Constant;
import org.xujin.doc.core.Property;
import org.xujin.doc.core.Schema;
import org.xujin.doc.core.exception.SchemaDesignException;
import org.xujin.doc.core.fragment.*;
import org.xujin.doc.core.supplier.Supplier;
import org.xujin.doc.core.type.HDClass;
import org.xujin.doc.core.type.HDType;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * 简单的模型师
 *
 * @author
 * @date 2018-05-18 11:15
 **/
public class SimpleModeler implements Modeler<ClassFragment> {

    @Override
    public Collection<ClassFragment> design(Archetype archetype) throws SchemaDesignException {
        final String comment = "Generated By Httpdoc";
        final String pkgGenerated = archetype.getPkg();
        final boolean pkgForced = archetype.isPkgForced();
        final Supplier supplier = archetype.getSupplier();
        final Schema schema = archetype.getSchema();
        final String pkgTranslated = schema.getPkg();
        final String pkg = pkgForced || pkgTranslated == null ? pkgGenerated : pkgTranslated;
        final String name = schema.getName();
        final ClassFragment clazz = new ClassFragment();
        clazz.setPkg(pkg);
        clazz.setCommentFragment(new CommentFragment(schema.getDescription() != null ? schema.getDescription() + "\n" + comment : comment));
        switch (schema.getCategory()) {
            case ENUM:
                clazz.setClazz(new HDClass(HDClass.Category.ENUM, (pkg == null || pkg.isEmpty() ? "" : pkg + ".") + name));
                Set<Constant> constants = schema.getConstants();
                for (Constant constant : (constants != null ? constants : Collections.<Constant>emptySet())) {
                    ConstantFragment con = new ConstantFragment(new CommentFragment(constant.getDescription()), constant.getName());
                    clazz.getConstantFragments().add(con);
                }
                break;
            case OBJECT:
                clazz.setClazz(new HDClass(HDClass.Category.CLASS, (pkg == null || pkg.isEmpty() ? "" : pkg + ".") + name));
                Schema superclass = schema.getSuperclass();
                clazz.setSuperclass(superclass != null && superclass.getCategory() == Category.OBJECT ? superclass.toType(pkgGenerated, pkgForced, supplier) : null);
                Map<String, Property> properties = schema.getProperties();
                for (Map.Entry<String, Property> entry : (properties != null ? properties.entrySet() : Collections.<Map.Entry<String, Property>>emptySet())) {
                    Property property = entry.getValue();
                    HDType type = property.getType().toType(pkgGenerated, pkgForced, supplier);
                    FieldFragment field = new FieldFragment();
                    field.setName(entry.getKey());
                    field.setType(type);
                    field.setCommentFragment(new CommentFragment(property.getDescription()));
                    clazz.getFieldFragments().add(field);

                    GetterMethodFragment getter = new GetterMethodFragment(type, entry.getKey());
                    clazz.getMethodFragments().add(getter);

                    SetterMethodFragment setter = new SetterMethodFragment(type, entry.getKey());
                    clazz.getMethodFragments().add(setter);
                }
                break;
        }
        return Collections.singleton(clazz);
    }

}
