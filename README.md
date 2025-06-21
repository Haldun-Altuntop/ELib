# E-Lib

# 1.12.13
* `DeveloperUtilities` sınıfı oluşturuldu.
* `DeveloperUtilities`: 
  - Uygulama debug sürümündeyken geliştiricinin testlerini kolaylaştırmak için gerekli değişkenleri içerir
* `ErrorActivity`: 
  - Hata mesajı eklendi (_NETWORK_ERROR = 2_)
  - Hata ayıklama sürümünde _NETWORK_ERROR_ hatası alırsa çevrimdışı mod için sorar.
* `Tools`: _showDialog(Context, String, String, String, String, DialogInterface.OnClickListener, DialogInterface.OnClickListener)_ metodu eklendi.
* `UncaughtExceptionHandler`: _ErrorActivity_'ye hata kodu gönderilir.
* `HomePageActivity`: Çevrimdışı mod için yalıtıldı.
    - Çevrimdışı mod etkin olduğunda güncellemeler kontrol edilmez.
    - Çevrimdışı mod etkin olduğunda çevrimdışı kullanıcı başlatılır.
    - Çevrimdışı mod etkin olduğunda istemci sürümü güncellenmez.

# 1.12.12
* `SplashScreenActivity`: Runnable objeleri _public_ ve _static_ yapıldı.
* `BookLoader`: Hizmet başlatılırken bağlantı kontrol edilir. `bkz: start()`

# 1.12.11
* `RegisterActivity`: Kayıt olma hatası düzeltildi.

# 1.12.10
* `SuspendedActivity` artık bağlantıyı kapatır.

# 1.12.9
* `ContributionFragment` artık işlevsel.

# 1.12.8
* `BookLoader` optimize edildi.

# 1.12.7
* `Client Version` artık Home Page Activity'de yapılır. (Eskiden Library Activity'deydi.)

# 1.12.6
* `Splash Screen Activity`'ye `arc.haldun.database.database.Manager` objesi eklendi.
* Kullanıcı durumu (online, offline) veritabanına gönderilir (`Home Page Activity`).

# 1.12.5
* Drawable olarak ünlem işareti eklendi (`baseline_warning_24`)
* `Suspended Activity`'ye ünlem işareti eklendi.

# 1.12.4
* **Book Loader**
    + `handler` final duruma getirildi.
    + Optimizasyon yapıldı.

# 1.12.3
* `Requests Activity` -> talep listesi boş oldupunda bilgilendirme mesajı gösterilir.
* `Splash Screen` -> Bilgilendirici yazının başlangıç içeriği uygulama adı olarak değiştirildi (önceden:TextView).
* `Book Loader` optimize edildi, yoruma alınmış kodlar kaldırıldı.
* **Library Activity**
    + `onPause` durumunda `Book Loader` durdurulur.
    + `positionChangeListener` işlemi iyileştirildi.

# 1.12.2
* `Library Activity`'nin parent aktivitesi `Home Page Activity` olarak ayarlandı.
* `Register Activity` içinde şifre onaylama kutusunun içindeki _pasword_ ibaresi _password again_ olarak değiştirildi.
* **Register Activity**
    + Şifre kontrolünün mantık akışı iyileştirildi.

# 1.12.1
* Ayarlar aktivitesine `Uygulama bilgileri` bölümü eklendi.
* `Card Item` sınıfına yeni fonksiyonlar eklendi:
    + `addTittle`: Mevcut başlık değişkeninin sonuna ekler.
    + `addSubTittle`: Mevcut altbaşlık değişkeninin sonuna ekler.

# 1.12.0
* Yeni aktiviteler oluşturuldu:
  * *OtherInformationActivity*
* **Book Details Activity:**
  * *Diğer Bilgiler* butonu artık kullanılabilir.

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
