# E-Lib

# 1.12.0 - prerelease6
* **Book Details Activity (Layout):** 
    + Kenur boşlukları azaltıldı.
    + Tasarım iyileştirildi.
* Müsait olmayan kitaplar kırmızı ile işaretlenir.
* **Book Loader:** Çalışmayan fonksiyonlar kaldırıldı.
* Kotlin bağımlılık sürüm uyuşmazlığı giderildi.
* **Library Activity:**
    + *Deprecated* ibaresi kaldırıldı.
    + Kitap ekleme butonu *user* rolündeki kullanıcılara gizlendi.
* **Priority** enum sınıfı oluşturuldu, **User** sınıfı içindeki roller bu enum'a taşındı.
* **Request Adapter:** *onClick* olayı iyileştirildi.
* **Request Activity:** Action bar eklendi.
* Yeni stringler eklendi.

# 1.12.0 - prerelease5
* Book Details Activity kullanıcı arayüzü yeniden tasarlandı.
* Book Ldader sınıfının aralığı 100 yapıldı.
* Library Activity üst bardaki geri tuşu işlevli hale getirildi.

# 1.12.0 - prerelease4
* Added images:
    - accept.xml
    - remove.xml

* **Activity Request (layout):**
    - Added action bar
    - Added recycler view
    - Added progress bar

* Created **_card_request.xml_**
* Created **Request Adapter**
* Created **Request Loader**
* Initialized **_Requests Activity_**

# 1.12.0 - prerelease2
* warnings fixed:

    + Book Adapter
        - Yapıcı metod (Activity, Book) açıklamasının parametreleri eklendi.
        - Yapıcı metodun (Activity, Book) _book_ parametresinin listeye eklenme biçimi değiştirildi (Arrays.asList() -> Collections.singletonList())
        - **onBindViewHolder(MyViewHolder, int):** holder.itemView.setOnClickListener lambda ifadesine dönüştürüldü.
        - **removeItem(int):** _UnsupportedOperationException_ fırlatıldı.
        - **reset():** _SuppressLint_ eklendi.
        * MyViewHolder(class):
            + **Yapıcı metod:** _btnDelete.setOnClickListener_ lamba ifadesine dönüştürüldü.
    
    + Bug Reporter
        - Değişkenler final yapıldı.
        - **Yapıcı metod(Context, Exception):** _SuppressWarnings_ eklendi.

    + Error File
        - Getter-setter metodları eklendi.
        - _SuppressWarnings_ eklendi.
    
    + Developer Activity
        - _Import_'lar optimize edildi.
        - **makeDialog(Context, CharSequence, CharSequence, CharSequence, DialogInterface.onClickListener):** Kaldırıldı.
    
    + External JAR Invoker:
        - _dexFolder_ ve _destinationDEX_ parametreleri final yapıldı.
        
    + Uncaught Exception Handler
        - **writeExceptionToFile(Throwable):** Dosya oluşturm işlemine kontrol noktası koyuldu.
        - **launchErrorActivity():** Kaldırıldı.

* **Home Page Activity:** Üst barın geri tuşu aktiflerştirildi.
* **Library Activity:** Üst barın geri tuşu kapatıldı.
* '_suspended\_user_' dizisi eklendi.
* **Library Activity:** 
    + `loadBooks()` removed.
    + `loadBooks2()` removed.
