String sqlQuery = " update load_element_details set " + 
         " LOAD_STATUS = ?, " +
         " start_ts = ? " +
         " where load_id = ? " + 
         " and elem_xml_name = ? ";
