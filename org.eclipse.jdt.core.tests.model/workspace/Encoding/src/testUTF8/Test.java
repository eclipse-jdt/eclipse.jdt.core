package testUTF8;
/**
 * <B>Farsi / Persian</B>: .من می توانم بدون احساس درد شيشه بخورم 
 * <B>Russian:</B> В чащах юга жил-был цитру? Да, но фальшивый кземплр! 
 * <B>Hungarian:</B> rvíztűrő tükörfúrógép. 
 * <B>Spanish:</B> El pingüino Wenceslao hizo kilómetros bajo exhaustiva 
 *  lluvia y frío, añoraba a su querido cachorro. 
 * <B>French:</B> Les naïfs ægithales hâtifs pondant à Noël où il gèle sont 
 *  sûrs d'être déçus et de voir leurs drôles d'œufs abîmés. </B>
 * <B>Esperanto:</B> Eĥoano ĉiuĵaŭde. 
 */
public class Test {
	public static void main(String[] args) {
		System.out.println("Some sentences using UTF-8 encoded characters:");
		System.out.println("Farsi / Persian</B>: .من می توانم بدون احساس درد شيشه بخورم");
		System.out.println("Russian:</B> В чащах юга жил-был цитру? Да, но фальшивый кземплр!");
		System.out.println("Hungarian:</B> rvíztűrő tükörfúrógép.");
		System.out.println("Spanish:</B> El pingüino Wenceslao hizo kilómetros bajo exhaustiva lluvia y frío, añoraba a su querido cachorro.");
		System.out.println("French:</B> Les naïfs ægithales hâtifs pondant à Noël où il gèle sont  sûrs d'être déçus et de voir leurs drôles d'œufs abîmés.");
		System.out.println("Esperanto:</B> Eĥoano ĉiuĵaŭde. ");
	}
}
