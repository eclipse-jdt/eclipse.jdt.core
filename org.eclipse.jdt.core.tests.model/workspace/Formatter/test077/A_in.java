    public IPropertyDescriptor[] getPropertyDescriptors() {
        if (getClass().equals(ClassDiagram.class)) {
            ComboBoxPropertyDescriptor cbd =
                new ComboBoxPropertyDescriptor(
                    ID_ROUTER,
                    OdinMessages.getString("..."), //$NON-NLS-1$
    new String[] {
        OdinMessages.getString("..."),
                OdinMessages.getString("...")
            });
            cbd.setLabelProvider(new ConnectionRouterLabelProvider());
            return new IPropertyDescriptor[] { cbd };
        }
        return super.getPropertyDescriptors();
    }