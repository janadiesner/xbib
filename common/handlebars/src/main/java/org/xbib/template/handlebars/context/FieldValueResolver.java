
package org.xbib.template.handlebars.context;

import org.xbib.template.handlebars.ValueResolver;
import org.xbib.template.handlebars.context.FieldValueResolver.FieldWrapper;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A specialization of {@link MemberValueResolver} with lookup and invocation
 * support for {@link java.lang.reflect.Field}.
 * It matches private, protected, package, public and no-static field.
 */
public class FieldValueResolver extends MemberValueResolver<FieldWrapper> {

    /**
     * Workaround for accessing to the public attribute length of arrays.
     */
    public interface FieldWrapper extends Member {
        /**
         * Returns the value of the field represented by this {@code Field}, on
         * the specified object. The value is automatically wrapped in an
         * object if it has a primitive type.
         * <p/>
         * <p/>
         * The underlying field's value is obtained as follows:
         * <p/>
         * <p/>
         * If the underlying field is a static field, the {@code obj} argument is ignored; it may be
         * null.
         * <p/>
         * <p/>
         * Otherwise, the underlying field is an instance field. If the specified {@code obj} argument
         * is null, the method throws a {@code NullPointerException}. If the specified object is not an
         * instance of the class or interface declaring the underlying field, the method throws an
         * {@code IllegalArgumentException}.
         * <p/>
         * <p/>
         * If this {@code Field} object enforces Java language access control, and the underlying field
         * is inaccessible, the method throws an {@code IllegalAccessException}. If the underlying field
         * is static, the class that declared the field is initialized if it has not already been
         * initialized.
         * <p/>
         * <p/>
         * Otherwise, the value is retrieved from the underlying instance or static field. If the field
         * has a primitive type, the value is wrapped in an object before being returned, otherwise it
         * is returned as is.
         * <p/>
         * <p/>
         * If the field is hidden in the type of {@code obj}, the field's value is obtained according to
         * the preceding rules.
         *
         * @param obj object from which the represented field's value is
         *            to be extracted
         * @return the value of the represented field in object {@code obj}; primitive values are
         * wrapped in an appropriate
         * object before being returned
         * @throws IllegalAccessException if the underlying field
         *                                is inaccessible.
         */
        Object get(Object obj) throws IllegalAccessException;
    }

    /**
     * Use a {@link java.lang.reflect.Field} as member.
     */
    private static class FieldMember extends AccessibleObject implements FieldWrapper {

        /**
         * The field object.
         */
        private Field field;

        /**
         * @param field The field object.
         */
        public FieldMember(final Field field) {
            this.field = field;
        }

        @Override
        public Class<?> getDeclaringClass() {
            return field.getDeclaringClass();
        }

        @Override
        public String getName() {
            return field.getName();
        }

        @Override
        public int getModifiers() {
            return field.getModifiers();
        }

        @Override
        public boolean isSynthetic() {
            return field.isSynthetic();
        }

        @Override
        public Object get(final Object obj) throws IllegalAccessException {
            return field.get(obj);
        }

        @Override
        public String toString() {
            return field.toString();
        }

        @Override
        public boolean isAccessible() {
            return field.isAccessible();
        }

        @Override
        public void setAccessible(final boolean flag) {
            field.setAccessible(flag);
        }
    }

    private static final class ArrayLengthMember implements FieldWrapper {

        /**
         * One instance is enough.
         */
        public static final FieldWrapper LENGTH = new ArrayLengthMember();

        /**
         * Not allowed.
         */
        private ArrayLengthMember() {
        }

        @Override
        public Class<?> getDeclaringClass() {
            return null;
        }

        @Override
        public String getName() {
            return "length";
        }

        @Override
        public int getModifiers() {
            return Modifier.PUBLIC;
        }

        @Override
        public boolean isSynthetic() {
            return false;
        }

        @Override
        public Object get(final Object obj) throws IllegalAccessException {
            return Array.getLength(obj);
        }

    }

    /**
     * The default value resolver.
     */
    public static final ValueResolver INSTANCE = new FieldValueResolver();

    @Override
    public boolean matches(final FieldWrapper field, final String name) {
        return !isStatic(field) && field.getName().equals(name);
    }

    @Override
    protected Object invokeMember(final FieldWrapper field, final Object context) {
        try {
            return field.get(context);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Shouldn't be illegal to access field '" + field.getName()
                            + "'", ex);
        }
    }

    @Override
    protected Set<FieldWrapper> members(final Class<?> clazz) {
        Set<FieldWrapper> members = new LinkedHashSet<FieldWrapper>();
        if (clazz.isArray()) {
            members.add(ArrayLengthMember.LENGTH);
        } else {
            Class<?> targetClass = clazz;
            do {
                Field[] fields = targetClass.getDeclaredFields();
                for (Field field : fields) {
                    FieldWrapper wrapper = new FieldMember(field);
                    if (matches(wrapper, memberName(wrapper))) {
                        members.add(wrapper);
                    }
                }
                targetClass = targetClass.getSuperclass();
            } while (targetClass != null && targetClass != Object.class);
        }
        return members;
    }

    @Override
    protected String memberName(final FieldWrapper member) {
        return member.getName();
    }
}
