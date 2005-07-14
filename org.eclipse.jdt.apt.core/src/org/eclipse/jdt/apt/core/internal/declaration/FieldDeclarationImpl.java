package org.eclipse.jdt.apt.core.internal.declaration;

import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.DeclarationVisitor;

public class FieldDeclarationImpl extends MemberDeclarationImpl implements FieldDeclaration
{
    public FieldDeclarationImpl(final IVariableBinding binding, final ProcessorEnvImpl env)
    {
        super(binding, env);
        assert binding.isField() : "binding doesn't represent a field"; //$NON-NLS-1$
    }
    
    public void accept(DeclarationVisitor visitor)
    {
        super.accept(visitor);
        visitor.visitFieldDeclaration(this);
    }

    public String getConstantExpression()
    {
        final IVariableBinding field = getDeclarationBinding();
        final Object constant = field.getConstantValue();
        if( constant == null ) return null;
        return constant.toString();   
    }

    public Object getConstantValue()
    {
        final IVariableBinding field = getDeclarationBinding();
        return field.getConstantValue();
    }

    public TypeDeclaration getDeclaringType()
    {
        final IVariableBinding field = getDeclarationBinding();
        final ITypeBinding outer = field.getDeclaringClass();
        return Factory.createReferenceType(outer, _env);
    }

    public String getSimpleName()
    {
		final IVariableBinding field = getDeclarationBinding();
        final String name = field.getName();
        return name == null ? "" : name; //$NON-NLS-1$
    }

    public TypeMirror getType()
    {
        final IVariableBinding field = getDeclarationBinding();
        final TypeMirror typeMirror = Factory.createTypeMirror( field.getType(), _env );
        if( typeMirror == null )
            return Factory.createErrorClassType(field.getType());
        return typeMirror;
    }

    public IVariableBinding getDeclarationBinding()
    {
        return (IVariableBinding)_binding;
    }
    
    public String toString()
    {
        return getSimpleName();
    }

    public MirrorKind kind(){ return MirrorKind.FIELD; }

    boolean isFromSource()
    {
        final ITypeBinding type = getDeclarationBinding().getDeclaringClass();
        return ( type != null && type.isFromSource() );
    }
}
