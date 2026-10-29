package javax.lang.model.util;

import java.util.List;
import java.util.ArrayList;
import javax.lang.model.element.*;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import static javax.lang.model.SourceVersion.*;

@SupportedSourceVersion(RELEASE_20)
public class ElementScanner14<R, P> extends ElementScanner9<R, P> {
    protected ElementScanner14(){
        super(null);
    }

    protected ElementScanner14(R defaultValue){
        super(defaultValue);
    }

    @Override
    public R visitType(TypeElement e, P p) {
        return scan(createScanningList(e, e.getEnclosedElements()), p);
    }

    public R visitExecutable(ExecutableElement e, P p) {
        return scan(createScanningList(e, e.getParameters()), p);
    }

    private List<? extends Element> createScanningList(Parameterizable element,
                                                       List<? extends Element> toBeScanned) {
        var typeParameters = element.getTypeParameters();
        if (typeParameters.isEmpty()) {
            return toBeScanned;
        } else {
            List<Element> scanningList = new ArrayList<>(typeParameters);
            scanningList.addAll(toBeScanned);
            return scanningList;
        }
    }

    @Override
    public R visitRecordComponent(RecordComponentElement e, P p) {
        return scan(e.getEnclosedElements(), p);
    }
}
