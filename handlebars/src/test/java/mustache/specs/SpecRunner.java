
package mustache.specs;

import org.junit.runner.Description;
import org.junit.runners.Parameterized;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpecRunner extends Parameterized {

    private List<String> labels;

    private Description labelledDescription;

    public SpecRunner(final Class<?> cl) throws Throwable {
        super(cl);
        initialiseLabels();
        generateLabelledDescription();
    }

    private void initialiseLabels() throws Exception {
        Collection<Object[]> parameterArrays = getParameterArrays();
        labels = new ArrayList<String>();
        for (Object[] parameterArray : parameterArrays) {
            Spec spec = (Spec) parameterArray[0];
            labels.add(spec.name());
        }
    }

    private Collection<Object[]> getParameterArrays() throws Exception {
        return getParameterArrays4_12();
    }

    private Collection<Object[]> getParameterArrays4_12() throws Exception {
        Object[][] methodCalls = new Object[][]{
                new Object[]{"getTestClass"},
                new Object[]{"getAnnotatedMethods", Class.class, Parameters.class},
                new Object[]{"get", int.class, 0},
                new Object[]{"invokeExplosively", Object.class, null, Object[].class, new Object[]{}}};
        return invokeMethodChain(this, methodCalls);
    }

    @SuppressWarnings("unchecked")
    private <T> T invokeMethodChain(Object object, final Object[][] methodCalls) throws Exception {
        for (Object[] methodCall : methodCalls) {
            String methodName = (String) methodCall[0];
            int parameterCount = (methodCall.length - 1) / 2;
            Class<?>[] classes = new Class<?>[parameterCount];
            Object[] arguments = new Object[parameterCount];
            for (int i = 1; i < methodCall.length; i += 2) {
                Class<?> cl = (Class<?>) methodCall[i];
                Object argument = methodCall[i + 1];
                int index = (i - 1) / 2; // messy!
                classes[index] = cl;
                arguments[index] = argument;
            }
            Method method = getDeclaredMethod(object.getClass(), methodName, classes);
            if (!method.isAccessible()) {
                if (Modifier.isPublic(method.getModifiers())) {
                    method.setAccessible(true);
                }
            }
            object = method.invoke(object, arguments);
        }
        return (T) object;
    }

    // iterates through super-classes until found. Throws NoSuchMethodException
    // if not
    private Method getDeclaredMethod(Class<?> cl, final String methodName,
                                     final Class<?>... parameterTypes) throws NoSuchMethodException {
        do {
            try {
                return cl.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                // do nothing - just fall through to the below
            }
            cl = cl.getSuperclass();
        } while (cl != null);
        throw new NoSuchMethodException("Method " + methodName + "() not found in hierarchy");
    }

    private void generateLabelledDescription() throws Exception {
        Description originalDescription = super.getDescription();
        labelledDescription = Description
                .createSuiteDescription(originalDescription.getDisplayName());
        ArrayList<Description> childDescriptions = originalDescription
                .getChildren();
        int childCount = childDescriptions.size();
        if (childCount != labels.size()) {
            throw new Exception(
                    "Number of labels and number of parameters must match.");
        }

        for (int i = 0; i < childDescriptions.size(); i++) {
            Description childDescription = childDescriptions.get(i);
            String label = labels.get(i);
            Description newDescription = Description
                    .createSuiteDescription(label);
            ArrayList<Description> grandChildren = childDescription
                    .getChildren();
            for (Description grandChild : grandChildren) {
                newDescription.addChild(grandChild);
            }
            labelledDescription.addChild(newDescription);
        }
    }

    @Override
    public Description getDescription() {
        return labelledDescription;
    }

}
