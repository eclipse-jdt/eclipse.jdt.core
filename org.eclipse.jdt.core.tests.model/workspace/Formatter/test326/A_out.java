public class A {
	/**
	 * @hibernate.set 
	 *  lazy="true" 
	 *  table="GUJArticleCategorization"
	 *  cascade="save-update" 
	 * @hibernate.collection-key 
	 *  column="articleId"
	 * @hibernate.collection-many-to-many 
	 *  class="br.com.guj.model.Category"
	 *  column="categoryId"
	 * 
	 * @return categorias deste artigo
	 */
	public Category getCategory() {
		return this.category;
	}
}