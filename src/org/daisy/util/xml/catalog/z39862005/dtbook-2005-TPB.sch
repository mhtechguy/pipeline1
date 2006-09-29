<?xml version="1.0" encoding="utf-8"?>
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron">

  <sch:title>DTBook 2005 Schematron tests for TPB</sch:title>

  <sch:ns prefix="dtbk" uri="http://www.daisy.org/z3986/2005/dtbook/"/>

  <!-- Rule 8: Only allow pagenum[@front] in frontmatter -->
  <sch:pattern name="dtbook_TPB_pageFront" id="dtbook_TPB_pageFront">
  	<sch:rule context="dtbk:pagenum[@page='front']">
  		<sch:assert test="ancestor::dtbk:frontmatter">[tpb08]&lt;pagenum page="front"/&gt; may only occur in &lt;frontmatter/&gt;</sch:assert>
  	</sch:rule>  	
  </sch:pattern>
    
  <!-- Rule 9: Disallow empty elements (with a few exceptions) -->
  <sch:pattern name="dtbook_TPB_emptyElements" id="dtbook_TPB_emptyElements">
  	<sch:rule context="dtbk:*">
  		<sch:report test="normalize-space(.)='' and not(*) and not(self::dtbk:img or self::dtbk:br or self::dtbk:meta or self::dtbk:link or self::dtbk:col or self::dtbk:th or self::dtbk:td or self::dtbk:dd)">[tpb09] Element may not be empty</sch:report>
  	</sch:rule>  	
  </sch:pattern>
  
  <!-- Rule 12: Frontmatter starts with doctitle and docauthor -->
  <sch:pattern name="dtbook_TPB_frontmatterStart" id="dtbook_TPB_frontmatterStart">
  	<sch:rule context="dtbk:frontmatter">
  		<sch:assert test="dtbk:*[1][self::dtbk:doctitle]">[tpb12] Frontmatter must begin with a doctitle element</sch:assert>
  	</sch:rule>
  	<sch:rule context="dtbk:frontmatter/dtbk:docauthor">
  		<sch:assert test="preceding-sibling::*[self::dtbk:doctitle or self::dtbk:docauthor]">[tpb12] Docauthor may only be preceded by doctitle</sch:assert>
  	</sch:rule>
  </sch:pattern>
  
  <!-- Rule 13: All documents must have frontmatter and bodymatter -->
  <sch:pattern name="dtbook_TPB_bookParts" id="dtbook_TPB_bookParts">
  	<sch:rule context="dtbk:book">
  		<sch:assert test="dtbk:frontmatter">[tpb13] A document must have frontmatter</sch:assert>
  		<sch:assert test="dtbk:bodymatter">[tpb13] A document must have bodymatter</sch:assert>
  	</sch:rule>  	
  </sch:pattern>  
  
  <!-- Rule 16: Targets of internal links must exist -->
  <sch:pattern name="dtbook_TPB_internalLinks" id="dtbook_TPB_internalLinks">
  	<sch:rule context="dtbk:a[starts-with(@href, '#')]">
  		<sch:assert test="count(//dtbk:*[@id=substring(current()/@href, 2)])=1">[tpb16] Targets of internal links must exist</sch:assert>
  	</sch:rule>  	
  </sch:pattern>  
  
  <!-- Rule 18: Disallow level -->
  <sch:pattern name="dtbook_TPB_noLevel" id="dtbook_TPB_noLevel">
  	<sch:rule context="dtbk:level">
  		<sch:report test="true()">[tpb18] Element level is not allowed</sch:report>
  	</sch:rule>  	
  </sch:pattern>  
  
  <!-- Rule 21: No nested tables -->
  <sch:pattern name="dtbook_TPB_nestedTables" id="dtbook_TPB_nestedTables">
  	<sch:rule context="dtbk:table">
  		<sch:report test="ancestor::dtbk:table">[tpb21] Nested tables are not allowed</sch:report>
  	</sch:rule>  	
  </sch:pattern> 
  
  <!-- Rule 22: Disallow cite -->
  <sch:pattern name="dtbook_TPB_noCite" id="dtbook_TPB_noCite">
  	<sch:rule context="dtbk:cite">
  		<sch:report test="true()">[tpb22] Element cite is not allowed</sch:report>
  	</sch:rule>  	
  </sch:pattern> 
  
  <!-- Rule 23: Increasing pagenum[@page='normal'] values -->
  <sch:pattern name="dtbook_TPB_pagenumIncrease" id="dtbook_TPB_pagenumIncrease">
  	<sch:rule context="dtbk:pagenum[@page='normal' and preceding::dtbk:pagenum[@page='normal']]">
  		<sch:assert test="number(current()) > number(preceding::dtbk:pagenum[@page='normal' and position()=1])">[tpb23] pagenum[@page='normal'] values must increase</sch:assert>
  	</sch:rule>  	
  </sch:pattern>
  
  <!-- Rule 24: Values of pagenum[@page='front'] must be unique -->
  <sch:pattern name="dtbook_TPB_pagenumUnique" id="dtbook_TPB_pagenumUnique">
  	<sch:rule context="dtbk:pagenum[@page='front']">
  		<sch:assert test="count(//dtbk:pagenum[@page='front' and string(.)=string(current())])=1">[tpb24] pagenum[@page='front'] values must be unique</sch:assert>
  	</sch:rule>  	
  </sch:pattern>
  
  <!-- Rule 26: Each note must have a noteref -->
  <sch:pattern name="dtbook_TPB_noteNoteref" id="dtbook_TPB_noteNoteref">
  	<sch:rule context="dtbk:note">
  		<sch:assert test="count(//dtbk:noteref[translate(@idref, '#', '')=current()/@id])>=1">[tpb26] Each note must have at least one noteref</sch:assert>
  	</sch:rule>  	
  </sch:pattern>
  
  <!-- Rule 27: Each annotation must have an annoref -->
  <sch:pattern name="dtbook_TPB_annotationAnnoref" id="dtbook_TPB_annotationAnnoref">
  	<sch:rule context="dtbk:annotation">
  		<sch:assert test="count(//dtbk:annoref[translate(@idref, '#', '')=current()/@id])>=1">[tpb27] Each annotation must have at least one annoref</sch:assert>
  	</sch:rule>  	
  </sch:pattern>  
  
  <!-- Rule 30: paragraph cannot be sibling with div@class=pgroup -->
  <sch:pattern name="dtbook_TPB_pgroupSibling" id="dtbook_TPB_pgroupSibling">
  	<sch:rule context="dtbk:div[@class='pgroup']">
  		<sch:report test="preceding-sibling::dtbk:p">[tpb30] paragraph group (&lt;div class="pgroup"&gt;) may not be sibling with a paragraph (&lt;p&gt;)</sch:report>
  		<sch:report test="following-sibling::dtbk:p">[tpb30] paragraph group (&lt;div class="pgroup"&gt;) may not be sibling with a paragraph (&lt;p&gt;)</sch:report>
  	</sch:rule>  	
  </sch:pattern>  
  
  <sch:pattern name="dtbook_TPB_IdValue" id="dtbook_TPB_IdValue">
  	<sch:rule context="*[@id]">
  		<sch:assert test="normalize-space(@id)=@id">ID attributes may not contain whitespace</sch:assert>
  	</sch:rule>
  </sch:pattern>
  
  <!-- TPB Structure Guidelines Validation -->
  
  <!-- Rule 35: Two letter codes in @xml:lang -->
  <sch:pattern name="dtbook_TPB_twoLetterXmlLang" id="dtbook_TPB_twoLetterXmlLang">
  	<sch:rule context="dtbk:*[@xml:lang]">
  		<sch:assert test="string-length(@xml:lang)=2 or string-length(@xml:lang)=5">[tpb35] xml:lang must have two-letter language codes.</sch:assert>
  		<sch:report test="string-length(@xml:lang)=2 and translate(@xml:lang, translate(@xml:lang, 'abcdefghijklmnopqrstuvwxyz', ''), '')!=@xml:lang">[tpb35] xml:lang language must be lowercase [a-z].</sch:report>
  		<sch:report test="string-length(@xml:lang)=5 and translate(substring(@xml:lang, 1, 2), translate(substring(@xml:lang, 1, 2), 'abcdefghijklmnopqrstuvwxyz', ''), '')!=substring(@xml:lang, 1, 2)">[tpb35] xml:lang language part must be lowercase [a-z].</sch:report>
  		<sch:report test="string-length(@xml:lang)=5 and translate(substring(@xml:lang, 4, 2), translate(substring(@xml:lang, 4, 2), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', ''), '')!=substring(@xml:lang, 4, 2)">[tpb35] xml:lang country part must be uppercase [A-Z].</sch:report>
  		<sch:report test="string-length(@xml:lang)=5 and substring(@xml:lang, 3, 1)!='-'">[tpb35] Separator between language part and country part in xml:lang must be '-'.</sch:report>
  	</sch:rule>  	
  </sch:pattern>  
  
  <!-- Rule 36: Class attributes of level1 in frontmatter -->
  <sch:pattern name="dtbook_TPB_frontmatterLevel1Class" id="dtbook_TPB_frontmatterLevel1Class">
  	<sch:rule context="dtbk:frontmatter/dtbk:level1">
  		<sch:assert test="@class='colophon' or @class='dedication' or @class='toc' or @class='briefToc' or @class='preface' or @class='introduction' or @class='glossary' or @class='other'">[tpb36] Class attribute must be one of: colophon, dedication, toc, briefToc, preface, introduction, glossary and other</sch:assert>
  	</sch:rule>
  </sch:pattern>
  
  <!-- Rule 37: Class attributes of level1 and level2 in bodymatter -->  
  <sch:pattern name="dtbook_TPB_bodymatterLevel1Level2Class" id="dtbook_TPB_bodymatterLevel1Level2Class">
  	<sch:rule context="dtbk:bodymatter/dtbk:level1">
  		<sch:assert test="@class='part' or @class='chapter'">[tpb37] Class attribute must be one of: part and chapter</sch:assert>
  		<!--<sch:report test="@class!=//dtbk:bodymatter/dtbk:level1/@class">[tpb37] All level1 class attributes in bodymatter must be either part or chapter</sch:report>-->
  		<sch:report test="@class!=preceding-sibling::dtbk:level1[1]/@class">[tpb37] All level1 class attributes in bodymatter must be either part or chapter</sch:report>
  		<sch:report test="@class='part' and dtbk:level2/@class!='chapter'">[tpb37] Level2 class attribute must be 'chapter' if level1 class attribute is 'part'</sch:report>
  	</sch:rule>
  </sch:pattern>
  
  <!-- Rule 38: Class attributes of level1 in rearmatter -->
  <sch:pattern name="dtbook_TPB_rearmatterLevel1Class" id="dtbook_TPB_rearmatterLevel1Class">
  	<sch:rule context="dtbk:rearmatter/dtbk:level1">
  		<sch:assert test="@class='bibliography' or @class='index' or @class='notes' or @class='glossary' or @class='appendix' or @class='backCovertext' or @class='other'">[tpb38] Class attribute must be one of: bibliography, index, notes, glossary, appendix, backCovertext and other</sch:assert>
  	</sch:rule>
  </sch:pattern>
  
  <!-- Rule 39: No class attributes on level[2-6] (unless level1 in bodymatter has @class="part") -->
  <sch:pattern name="dtbook_TPB_noLevel26ClassAttrs" id="dtbook_TPB_noLevel26ClassAttrs">
    <sch:rule context="dtbk:level2">
  		<sch:report test="parent::dtbk:level1/@class!='part' and @class">[tpb39] Class attribute on level2 not allowed unless level1 class attribute is 'part'</sch:report>
  	</sch:rule>
  	<sch:rule context="dtbk:*[self::dtbk:level3 or self::dtbk:level4 or self::dtbk:level5 or self::dtbk:level6]">
  		<sch:assert test="not(@class)">[tpb39] No class attributes on level2-level6</sch:assert>
  	</sch:rule>
  </sch:pattern>
  
  <!-- Rule 40: No page numbering gaps -->
  <sch:pattern name="dtbook_TPB_pagenumNoGap" id="dtbook_TPB_pagenumNoGap">
  	<sch:rule context="dtbk:pagenum[@page='normal']">
  		<sch:report test="preceding::dtbk:pagenum[@page='normal'] and number(preceding::dtbk:pagenum[@page='normal'][1]) != number(.)-1">[tpb40] No gaps may occur in page numbering</sch:report>
  	</sch:rule>
  </sch:pattern>
  
  <!-- Rule 41 & 42: pagenum id attribute -->
  <sch:pattern name="dtbook_TPB_idPagenum" id="dtbook_TPB_idPagenum">
  	<sch:rule context="dtbk:pagenum">
  		<!-- 41 -->
  		<sch:assert test="starts-with(@id, 'page-') or starts-with(@id, 'unnum-')">[tpb41] pagenum must have ID on the form page-[number] or unnum-[number].</sch:assert>
  		<sch:report test="starts-with(@id, 'page-') and substring(@id, 6)!=.">[tpb41] ID must be on the form page-[number]</sch:report>
  		<sch:report test="starts-with(@id, 'unnum-') and substring(@id, 7)!=.">[tpb41] ID must be on the form unnum-[number]</sch:report>
  		<!-- 42 -->
  		<sch:report test="starts-with(@id, 'unnum-') and @page!='special'">[tpb42] Unnumbered pages nust be of type @page="special".</sch:report>
  		<sch:report test="starts-with(@id, 'unnum-') and lang('sv') and substring(@id, 7)!='Onumrerad sida'">[tpb42] Value of unnumbered pages must be 'Onumrerad sida' in swedish context.</sch:report>
  		<sch:report test="starts-with(@id, 'unnum-') and lang('en') and substring(@id, 7)!='Unnumbered page'">[tpb42] Value of unnumbered pages must be 'Unnumbered page' in swedish context.</sch:report>
  	</sch:rule>
  </sch:pattern>
  
  <!-- Rule 43: dc:Publisher must be 'TPB' -->
  <sch:pattern name="dtbook_TPB_metaDcublisher" id="dtbook_TPB_metaDcublisher">
    <sch:rule context="dtbk:head">      
      <!-- dc:Publisher -->
      <sch:assert test="count(dtbk:meta[@name='dc:Publisher' and @content='TPB'])=1">[tpb43] Meta dc:Publisher must exist and have value 'TPB'</sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <!-- Rule 44: Two letter codes in dc:Language -->
  <sch:pattern name="dtbook_TPB_twoLetterDcLanguage" id="dtbook_TPB_twoLetterDcLanguage">
  	<sch:rule context="dtbk:head">
  		<sch:assert test="count(dtbk:meta[@name='dc:Language'])>=1">[tpb44] Meta dc:Language must exist</sch:assert>
  	</sch:rule>
  	<sch:rule context="dtbk:head/dtbk:meta[@name='dc:Language']">
  		<sch:assert test="string-length(@content)=2 or string-length(@content)=5">[tpb44] Meta dc:Language must have two-letter language code.</sch:assert>
  		<sch:report test="string-length(@content)=2 and translate(@content, translate(@content, 'abcdefghijklmnopqrstuvwxyz', ''), '')!=@content">[tpb44] Meta dc:Language language must be lowercase [a-z].</sch:report>
  		<sch:report test="string-length(@content)=5 and translate(substring(@content, 1, 2), translate(substring(@content, 1, 2), 'abcdefghijklmnopqrstuvwxyz', ''), '')!=substring(@content, 1, 2)">[tpb44] Meta dc:Language language part must be lowercase [a-z].</sch:report>
  		<sch:report test="string-length(@content)=5 and translate(substring(@content, 4, 2), translate(substring(@content, 4, 2), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', ''), '')!=substring(@content, 4, 2)">[tpb44] Meta dc:Language country part must be uppercase [A-Z].</sch:report>
  		<sch:report test="string-length(@content)=5 and substring(@content, 3, 1)!='-'">[tpb44] Separator between language part and country part in meta dc:Language must be '-'.</sch:report>
  	</sch:rule>  	
  </sch:pattern>
  
  <!-- Rule 45: noteref and note must have the same class attribute -->
  <sch:pattern name="dtbook_TPB_noterefNoteClass" id="noterefNoteClass">
    <sch:rule context="dtbk:noteref">
    	<!-- Support both IDREF and URI specification of @idref -->
    	<sch:report test="@class!=//dtbk:note[@id=translate(current()/@idref, '#', '')]/@class">[tpb45] note and noteref must have the same class attribute</sch:report>
    </sch:rule>
  </sch:pattern>
  
  <!-- Rule 46: Allowed class attribute values of note -->
  <sch:pattern name="dtbook_TPB_allowedNoteClass" id="dtbook_TPB_allowedNoteClass">
    <sch:rule context="dtbk:note">
    	<sch:assert test="@class='endnote' or @class='rearnote'">[tpb46] class attribute for note must be 'endnote' or 'rearnote'</sch:assert>
    	<sch:report test="@class='rearnote' and (not(ancestor::dtbk:rearmatter) or not(ancestor::dtbk:level1[@class='notes']))">[tpb46] Rearnotes must be in level1@class='notes' in rearmatter</sch:report>
    	<sch:report test="@class='endnote' and ancestor::dtbk:rearmatter and ancestor::dtbk:level1[@class='notes']">[tpb46] Endnotes may not be in level1@class='notes' in rearmatter</sch:report>
    	<sch:report test="@class='endnote' and not(parent::dtbk:level1[@class='chapter'] or parent::dtbk:level2[@class='chapter'])">[tpb46] Endnotes must be placed at the end of a chapter</sch:report>
    </sch:rule>
  </sch:pattern>
  
  <!-- Rule 48: Headings in notes section in rearmatter -->
  <sch:pattern name="dtbook_TPB_headingsInNotesSection" id="dtbook_TPB_headingsInNotesSection">
    <sch:rule context="dtbk:rearmatter/dtbk:level1[@class='notes']/dtbk:level2/dtbk:h2">    
    	<sch:assert test="count(//dtbk:bodymatter//dtbk:level2[@class='chapter']/dtbk:h2[.=current()]) + 
    	                  count(//dtbk:bodymatter//dtbk:level1[@class='chapter']/dtbk:h2[.=current()]) = 1"
    	   >[tpb48] Heading in notes section does not exist in the bodymatter of the book</sch:assert>
    </sch:rule>    
    <sch:rule context="dtbk:rearmatter/dtbk:level1[@class='notes']/dtbk:level2/dtbk:note">    
          <sch:assert test="count(//dtbk:level2[@class='chapter' and descendant::dtbk:noteref[translate(@idref,'#','')=current()/@id] and dtbk:h2=current()/parent::dtbk:level2/dtbk:h2]) +
                            count(//dtbk:level1[@class='chapter' and descendant::dtbk:noteref[translate(@idref,'#','')=current()/@id] and dtbk:h1=current()/parent::dtbk:level2/dtbk:h2]) >= 1"
           >[tpb48] There is no note reference to this note in the corresponding section in the bodymatter</sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <!-- Rule 49: Sidebar must have @render="optional" -->
  <sch:pattern name="dtbook_TPB_renderSidebar" id="dtbook_TPB_renderSidebar">
    <sch:rule context="dtbk:sidebar">
    	<sch:assert test="@render='optional'">[tpb49] sidebar must have a 'render' attribute equal to 'optional'</sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <!-- Rule 50: imgage alt attribute -->
  <sch:pattern name="dtbook_TPB_imgAlt" id="dtbook_TPB_imgAlt">
    <sch:rule context="dtbk:img">
    	<sch:report test="lang('sv') and @alt!='illustration'">[tpb50] an image in swedish language context must have attribute alt="illustration"</sch:report>
    	<sch:report test="lang('en') and @alt!='image'">[tpb50] an image in english language context must have attribute alt="image"</sch:report>    	
    </sch:rule>
  </sch:pattern>
  
  <!-- Rule 51 & 52: -->
  <sch:pattern name="dtbook_TPB_imgNames" id="dtbook_TPB_imgNames">
    <sch:rule context="dtbk:img">
    	<sch:assert test="contains(@src,'.jpg') and substring-after(@src,'.jpg')=''">[tpb52] Images must have the .jpg file extension.</sch:assert>
    	<sch:report test="contains(@src,'.jpg') and string-length(@src)=4">[tpb52] Images must have a base name, not just an extension.</sch:report>
    	<sch:report test="contains(@src,'/')">[tpb51] Images must be in the same folder as the DTBook file.</sch:report>
    	<sch:assert test="string-length(translate(substring(@src,1,string-length(@src)-4),'-_abcdefghijklmnopqrstuvwxyz0123456789',''))=0">[tpb52] Image file name contains an illegal character (must be -_a-z0-9).</sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <!-- Rule 53: caption in imggoup -->
  <sch:pattern name="dtbook_TPB_captionInImggroup" id="dtbook_TPB_captionInImggroup">
    <sch:rule context="dtbk:imggroup/dtbk:caption">
    	<sch:assert test="preceding-sibling::*[1][self::dtbk:img]">[tpb53] caption must immediately follow an img element</sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <!-- Rule 54: prodnote in imggoup -->
  <sch:pattern name="dtbook_TPB_prodnoteInImggroup" id="dtbook_TPB_prodnoteInImggroup">
    <sch:rule context="dtbk:imggroup/dtbk:prodnote">
    	<sch:assert test=".='Bildbeskrivning'">[tpb54] Value of prodnote in imggroup must be 'Bildbeskrivning'</sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <!-- Rule 55: no prodnotes as direct children of list -->
  <sch:pattern name="dtbook_TPB_prodnoteInList" id="dtbook_TPB_prodnoteInList">
    <sch:rule context="dtbk:list">
    	<sch:assert test="not(dtbk:prodnote)">[tpb55] prodnotes are not allowed in lists</sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <!-- Rule 56: enum attribute only on numbered lists -->
  <sch:pattern name="dtbook_TPB_enumAttrInList" id="dtbook_TPB_enumAttrInList">
    <sch:rule context="dtbk:list">
    	<sch:report test="@enum and @type!='ol'">[tpb56] enum attribute only allowed in numbered lists</sch:report>
    </sch:rule>
  </sch:pattern>
  
  <!-- Rule 57 & 58: lic in table of contents -->
  <sch:pattern name="dtbook_TPB_licInToc" id="dtbook_TPB_licInToc">
    <!-- 57 -->
    <sch:rule context="dtbk:frontmatter/dtbk:level1[@class='toc']">
    	<sch:assert test="dtbk:list">[tpb57] table of contents must have a list</sch:assert>
    </sch:rule>
    <sch:rule context="dtbk:frontmatter/dtbk:level1[@class='toc']/dtbk:list/dtbk:li">
    	<sch:assert test="child::*[self::dtbk:lic and (@class='entry' or @class='pagenum')]">[tpb57] list items in table of contents must only have lic children having class attribute 'entry' or 'pagenum'</sch:assert>
    	<sch:assert test="normalize-space(text())=''">[tpb57] list items in table of contents must only contain lic elements</sch:assert>
    </sch:rule>
    <!-- 58 -->
    <sch:rule context="dtbk:frontmatter/dtbk:level1[@class='toc']/dtbk:list/dtbk:li/dtbk:lic[@class='pagenum']">
    	<sch:assert test=".=//dtbk:pagenum">[tpb58] there must exist a pagenum element for each lic@class="pagenum"</sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <!-- Rule 59: No pagegenum between a term and a definition in definition lists -->
  <sch:pattern name="dtbook_TPB_noDtPagenumDD" id="dtbook_TPB_noDtPagenumDD">
    <sch:rule context="dtbk:dl/dtbk:pagenum">
    	<sch:assert test="preceding-sibling::*[1][self::dtbk:dd] and following-sibling::*[1][self::dtbk:dt]">[tpb59] pagenum in definition list must occur between dd and dt</sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <!-- Rule 60: pagenum only in first cell of a row in a table -->
  <sch:pattern name="dtbook_TPB_pagenumInTable" id="dtbook_TPB_pagenumInTable">
    <sch:rule context="dtbk:*[self::dtbk:th or self::dtbk:td]">
    	<sch:report test="descendant::dtbk:pagenum and count(preceding-sibling::*[self::dtbk:th or self::dtbk:td])!=0">[tpb60] pagenum element must be in the first cell of a table row</sch:report>
    </sch:rule>
  </sch:pattern>
  
  <!-- Rule 61: Heading for the notes section -->
  <sch:pattern name="dtbook_TPB_h1NotesSection" id="dtbook_TPB_h1NotesSection">
    <sch:rule context="dtbk:rearmatter/dtbk:level1[@class='notes']">
    	<sch:report test="lang('sv') and h1!='Noter'">[tpb61] Heading of notes section must be 'Noter' (swedish)</sch:report>
    	<sch:report test="lang('en') and h1!='Notes'">[tpb61] Heading of notes section must be 'Notes' (english)</sch:report>
    </sch:rule>
  </sch:pattern>
  
  <!-- Rule 62: Terms and definitions in definition lists must come in pairs -->
  <sch:pattern name="dtbook_TPB_dtDdPairs" id="dtbook_TPB_dtDdPairs">
    <sch:rule context="dtbk:dl/dtbk:dt">
      <sch:assert test="following-sibling::*[1][self::dtbk:dd]">[tpb62] Data terms and data definitions come in pairs</sch:assert>
    </sch:rule>
    <sch:rule context="dtbk:dl/dtbk:dd">
      <sch:assert test="preceding-sibling::*[1][self::dtbk:dt]">[tpb62] Data terms and data definitions come in pairs</sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <sch:pattern id="dtbook_noBlockInInline" name="dtbook_noBlockInInline">
  	<sch:rule context="dtbk:*[self::dtbk:address    or self::dtbk:annotation or self::dtbk:author   or 
  	                          self::dtbk:blockquote or self::dtbk:bridgehead or self::dtbk:caption  or 
  	                          self::dtbk:dateline   or self::dtbk:div        or self::dtbk:dl       or
  	                          self::dtbk:epigraph   or self::dtbk:hd         or self::dtbk:linegoup or 
  	                          self::dtbk:list       or self::dtbk:note       or self::dtbk:p        or 
  	                          self::dtbk:poem       or self::dtbk:sidebar    or self::dtbk:table    or 
  	                          self::dtbk:title]">
  	  <sch:report test="ancestor::dtbk:a      or ancestor::dtbk:abbr or ancestor::dtbk:acronym or ancestor::dtbk:annoref or 
  	                    ancestor::dtbk:bdo    or ancestor::dtbk:code or ancestor::dtbk:dfn     or ancestor::dtbk:em      or 
  	                    ancestor::dtbk:kbd or ancestor::dtbk:linenum or ancestor::dtbk:noteref or 
  	                    ancestor::dtbk:q      or ancestor::dtbk:samp or ancestor::dtbk:sent    or ancestor::dtbk:span    or 
  	                    ancestor::dtbk:strong or ancestor::dtbk:sub  or ancestor::dtbk:sup     or ancestor::dtbk:w">Block element used in inline context</sch:report>
  	</sch:rule>
  </sch:pattern>
  
  <sch:pattern id="dtbook_noBlockSiblingWithInline" name="dtbook_noBlockSiblingWithInline">
  	<sch:rule context="dtbk:*[self::dtbk:address    or self::dtbk:annotation or self::dtbk:author   or 
  	                          self::dtbk:blockquote or self::dtbk:bridgehead or self::dtbk:caption  or 
  	                          self::dtbk:dateline   or self::dtbk:div        or self::dtbk:dl       or
  	                          self::dtbk:epigraph   or self::dtbk:hd         or self::dtbk:linegoup or 
  	                          self::dtbk:list       or self::dtbk:note       or self::dtbk:p        or 
  	                          self::dtbk:poem       or self::dtbk:sidebar    or self::dtbk:table    or 
  	                          self::dtbk:title]">
  	  <sch:report test="following-sibling::dtbk:a      or following-sibling::dtbk:abbr or following-sibling::dtbk:acronym or following-sibling::dtbk:annoref or 
  	                    following-sibling::dtbk:bdo    or following-sibling::dtbk:code or following-sibling::dtbk:dfn     or following-sibling::dtbk:em      or 
  	                    following-sibling::dtbk:kbd or following-sibling::dtbk:linenum or following-sibling::dtbk:noteref or 
  	                    following-sibling::dtbk:q      or following-sibling::dtbk:samp or following-sibling::dtbk:sent    or following-sibling::dtbk:span    or 
  	                    following-sibling::dtbk:strong or following-sibling::dtbk:sub  or following-sibling::dtbk:sup     or following-sibling::dtbk:w       or
  	                    normalize-space(following-sibling::text())!=''">Block element as sibling to inline element</sch:report>
  	  <sch:report test="preceding-sibling::dtbk:a      or preceding-sibling::dtbk:abbr or preceding-sibling::dtbk:acronym or preceding-sibling::dtbk:annoref or 
  	                    preceding-sibling::dtbk:bdo    or preceding-sibling::dtbk:code or preceding-sibling::dtbk:dfn     or preceding-sibling::dtbk:em      or 
  	                    preceding-sibling::dtbk:kbd or preceding-sibling::dtbk:linenum or preceding-sibling::dtbk:noteref or 
  	                    preceding-sibling::dtbk:q      or preceding-sibling::dtbk:samp or preceding-sibling::dtbk:sent    or preceding-sibling::dtbk:span    or 
  	                    preceding-sibling::dtbk:strong or preceding-sibling::dtbk:sub  or preceding-sibling::dtbk:sup     or preceding-sibling::dtbk:w       or
  	                    normalize-space(preceding-sibling::text())!=''">Block element as sibling to inline element</sch:report>                  
  	</sch:rule>
  </sch:pattern>
  
  <!-- Rule 65: No border attribute on tables -->
  <sch:pattern name="dtbook_TPB_noTableBorder" id="dtbook_TPB_noTableBorder">
    <sch:rule context="dtbk:table">
    	<sch:report test="@border">[tpb65] Border attributes on tables is not allowed</sch:report>
    </sch:rule>
  </sch:pattern>
  
  <!-- Rule 67: doctitle and docauthor only allowed in frontmatter -->
  <sch:pattern name="dtbook_TPB_titleAuthorInFront" id="dtbook_TPB_titleAuthorInFront">
    <sch:rule context="dtbk:doctitle">
    	<sch:assert test="parent::dtbk:frontmatter">[tpb67] doctitle is only allowed in frontmatter</sch:assert>
    </sch:rule>
    <sch:rule context="dtbk:docauthor">
    	<sch:assert test="parent::dtbk:frontmatter">[tpb67] docauthor is only allowed in frontmatter</sch:assert>
    </sch:rule>    
  </sch:pattern>
  
  <!-- Rule 68: No smilref attributes -->
  <sch:pattern name="dtbook_TPB_noSmilref" id="dtbook_TPB_noSmilref">
    <sch:rule context="dtbk:*/@smilref">
    	<sch:assert test="false()">[tpb68] smilref attributes in a plain DTBook file is not allowed</sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <!-- Rule 69: depth attribute of list elements -->
  <sch:pattern name="dtbook_TPB_depthList" id="dtbook_TPB_depthList">
    <sch:rule context="dtbk:list">
    	<sch:report test="@depth and @depth!=count(ancestor-or-self::dtbk:list)">[tpb69] depth attribute on list element must contain the list wrapping level</sch:report>
    </sch:rule>
  </sch:pattern>
  
  <!-- Rule 70: Heading for the colophon -->
  <sch:pattern name="dtbook_TPB_colophonHeading" id="dtbook_TPB_colophonHeading">
    <sch:rule context="dtbk:frontmatter/dtbk:level1[@class='colophon']">
    	<sch:report test="lang('sv') and h1!='Kolofon'">[tpb70] Heading of colophon must be 'Kolofon' (swedish)</sch:report>
    	<sch:report test="lang('en') and h1!='Colophon'">[tpb70] Heading of colophon must be 'Colophon' (english)</sch:report>
    </sch:rule>
  </sch:pattern>
 
  <!-- Rule 73: headers attribute on table cells -->
  <sch:pattern name="dtbook_TPB_headersThTd" id="dtbook_TPB_headersThTd">
    <sch:rule context="dtbk:th">
    	<sch:report test="@headers">[tpb73] Headers attribute may only exist on 'td' cells.</sch:report>
    </sch:rule>
    <sch:rule context="dtbk:td[@headers]">
    	<sch:assert test="
    		count(
    			ancestor::dtbk:table[1]//dtbk:th/@id[contains( concat(' ',current()/@headers,' '), concat(' ',normalize-space(),' ') )]
			) = 
			string-length(normalize-space(@headers)) - string-length(translate(normalize-space(@headers), ' ','')) + 1
		">[tpb73] Not all the tokens in the headers attribute match the id attributes of 'th' elements in that table.</sch:assert>
	</sch:rule>
  </sch:pattern>
  
  <!-- Rule 74: imgref attribute on prodnote -->
  <sch:pattern name="dtbook_TPB_imgrefProdnote" id="dtbook_TPB_imgrefProdnote">
    <sch:rule context="dtbk:prodnote[@imgref]">
    	<sch:assert test="
    		count(
    			parent::dtbk:imggroup//dtbk:img/@id[contains( concat(' ',current()/@imgref,' '), concat(' ',normalize-space(),' ') )]
			) = 
			string-length(normalize-space(@imgref)) - string-length(translate(normalize-space(@imgref), ' ','')) + 1
		">[tpb74] Not all the tokens in the imgref attribute match the id attributes of 'img' elements in that imggroup.</sch:assert>
	</sch:rule>
  </sch:pattern>
  
  <!-- Rule 75: imgref attribute on caption -->
  <sch:pattern name="dtbook_TPB_imgrefCaption" id="dtbook_TPB_imgrefCaption">
    <sch:rule context="dtbk:caption[@imgref]">
    	<sch:assert test="
    		count(
    			parent::dtbk:imggroup//dtbk:img/@id[contains( concat(' ',current()/@imgref,' '), concat(' ',normalize-space(),' ') )]
			) = 
			string-length(normalize-space(@imgref)) - string-length(translate(normalize-space(@imgref), ' ','')) + 1
		">[tpb75] Not all the tokens in the imgref attribute match the id attributes of 'img' elements in that imggroup.</sch:assert>
	</sch:rule>
  </sch:pattern>
        
  <!-- Rule 76: accesskey and tabindex attribute on a -->
  <sch:pattern name="dtbook_TPB_accesskeyTabindex" id="dtbook_TPB_accesskeyTabindex">
    <sch:rule context="dtbk:a">
    	<sch:report test="@accesskey and string-length(@accesskey)!=1">[tpb76] accesskey attribute on &lt;a&gt; elements may only be one character long</sch:report>
    	<sch:report test="@tabindex and string-length(translate(@width,'0123456789',''))!=0">[tpb76] tabindex of &lt;a&gt; elements must be expressed in numbers only</sch:report>
    	<sch:report test="@accesskey and count(//dtbk:a/@accesskey=@accesskey)!=1">[tpb76] accesskey attribute values must be unique within the document</sch:report>
    	<sch:report test="@tabindex and count(//dtbk:a/@tabindex=@tabindex)!=1">[tpb76] tabindex attribute values must be unique within the document</sch:report>
    </sch:rule>
  </sch:pattern>      
        
  <!-- Rule 77: char attribute of col, colgroup, tbody, td, tfoot, th, thead, tr -->
  <sch:pattern name="dtbook_TPB_charAttribute" id="dtbook_TPB_charAttribute">
    <sch:rule context="dtbk:*[self::dtbk:col   or self::dtbk:colgroup or self::dtbk:tbody or self::dtbk:td or 
                              self::dtbk:tfoot or self::dtbk:th       or self::dtbk:thead or self::dtbk:tr]">
    	<sch:report test="@char and string-length(@char)!=1">[tpb77] length of char attribute value must be one</sch:report>
    	<sch:report test="@char and @align!='char'">[tpb77] char attribute may only occur when align attribute value is 'char'</sch:report>
    	<sch:report test="@charoff and not(@char)">[tpb77] char offset attribute may only occur when align attribute value is 'char'</sch:report>
    	<sch:report test="@charoff and translate(@charoff,'0123456789','')!=''">[tpb77] char offset attribute value must be expressed in numbers</sch:report>
    </sch:rule>
  </sch:pattern>
        
  <!-- Rule 79: width and height attributes of img -->
  <sch:pattern name="dtbook_TPB_imgWidthHeight" id="dtbook_TPB_imgWidthHeight">
    <sch:rule context="dtbk:img">
    	<sch:report test="@width and string-length(translate(@width,'0123456789',''))!=0">[tpb79] width of images must be expressed in numbers only</sch:report>
    	<sch:report test="@height and string-length(translate(@width,'0123456789',''))!=0">[tpb79] height of images must be expressed in numbers only</sch:report>
    </sch:rule>
  </sch:pattern>
  
  <!-- Rule 80: attributes on the table element -->
  <sch:pattern name="dtbook_TPB_tableAttributes" id="dtbook_TPB_tableAttributes">
    <sch:rule context="dtbk:table">
    	<sch:assert test="not(@width) or 
    	                  string-length(translate(@width,'0123456789',''))=0 or
    	                  (contains(@width,'%') and substring-after(@width,'%')='' and translate(@width,'%0123456789','')='' and string-length(@width)>=2 and (string-length(@width)&lt;=3 or @width='100%') )"
    	   >[tpb80] width of tables must be expressed in pixels or percentage in interval [0%-100%]</sch:assert>
   	 	<sch:assert test="not(@cellspacing) or 
    	                  string-length(translate(@cellspacing,'0123456789',''))=0 or
    	                  (contains(@cellspacing,'%') and substring-after(@cellspacing,'%')='' and translate(@cellspacing,'%0123456789','')='' and string-length(@cellspacing)>=2 and (string-length(@cellspacing)&lt;=3 or @cellspacing='100%') )"
    	   >[tpb80] cellspacing of tables must be expressed in pixels or percentage in interval [0%-100%]</sch:assert>
		<sch:assert test="not(@cellpadding) or 
    	                  string-length(translate(@cellpadding,'0123456789',''))=0 or
    	                  (contains(@cellpadding,'%') and substring-after(@cellpadding,'%')='' and translate(@cellpadding,'%0123456789','')='' and string-length(@cellpadding)>=2 and (string-length(@cellpadding)&lt;=3 or @cellpadding='100%') )"
    	   >[tpb80] cellpadding of tables must be expressed in pixels or percentage in interval [0%-100%]</sch:assert>
    </sch:rule>
  </sch:pattern> 
        
  <!-- Rule 88: start attribute only on numbered lists -->
  <sch:pattern name="dtbook_TPB_startAttrInList" id="dtbook_TPB_startAttrInList">
    <sch:rule context="dtbk:list">
    	<sch:report test="@start and @type!='ol'">[tpb88] start attribute only allowed in numbered lists</sch:report>
    	<sch:report test="@start='' or string-length(translate(@start,'0123456789',''))!=0">[tpb88] start attribute must be a number</sch:report>
    </sch:rule>
  </sch:pattern>  
  
  <!-- Rule 89: Verify dc-metadata names -->
  <sch:pattern name="dtbook_TPB_dcMetadata" id="dtbook_TPB_dcMetadata">
    <sch:rule context="dtbk:meta">
    	<sch:report test="starts-with(@name, 'dc:') and not(@name='dc:Title' or @name='dc:Subject' or @name='dc:Description' or
    	                                                    @name='dc:Type' or @name='dc:Source' or @name='dc:Relation' or 
    	                                                    @name='dc:Coverage' or @name='dc:Creator' or @name='dc:Publisher' or 
    	                                                    @name='dc:Contributor' or @name='dc:Rights' or @name='dc:Date' or 
    	                                                    @name='dc:Format' or @name='dc:Identifier' or @name='dc:Language')"
                          >[tpb89] Incorrect Dublin core metadata name</sch:report>
       <sch:report test="starts-with(@name, 'DC:') or starts-with(@name, 'Dc:') or starts-with(@name, 'dC:')">[tpb89] Incorrect Dublin core metadata prefix</sch:report>
    </sch:rule>
  </sch:pattern>
  
  <!-- Rule 93: Some elements may not start of end with whitespace -->
  <sch:pattern name="dtbook_TPB_trimmedWhitespace" id="dtbook_TPB_trimmedWhitespace">
    <sch:rule context="dtbk:*[self::dtbk:h1 or self::dtbk:h2 or self::dtbk:h3 or self::dtbk:h4 or self::dtbk:h5 or self::dtbk:h6 or self::dtbk:hd]">
    	<sch:report test="normalize-space(substring(.,1,1))=''">[tpb93] element <name/> may not have leading whitespace</sch:report>
    	<sch:report test="normalize-space(substring(.,string-length(.),1))=''">[tpb93] element <name/> may not have trailing whitespace</sch:report>
    </sch:rule>
  </sch:pattern>  
  
  <!-- Rule 94: span attribute on col and colgroup elements -->
  <sch:pattern name="dtbook_TPB_spanColColgroup" id="dtbook_TPB_spanColColgroup">
    <sch:rule context="dtbk:*[self::dtbk:col or self::dtbk:colgroup]">
    	<sch:report test="@span and (translate(@span,'0123456789','')!='' or starts-with(@span,'0'))">[tpb94] span on <name/> element must be numeric</sch:report>
    </sch:rule>
  </sch:pattern>
  
  <!-- Rule 95: rowspan and colspan on td/th elements -->
  <sch:pattern name="dtbook_TPB_rowspanColspan" id="dtbook_TPB_rowspanColspan">
    <sch:rule context="dtbk:*[self::dtbk:td or self::dtbk:th]">
    	<sch:report test="@rowspan and (translate(@rowspan,'0123456789','')!='' or starts-with(@rowspan,'0'))">[tpb95] rowspan on <name/> element must be numeric</sch:report>
    	<sch:report test="@colspan and (translate(@colspan,'0123456789','')!='' or starts-with(@colspan,'0'))">[tpb95] colspan on <name/> element must be numeric</sch:report>
    	
    	<sch:report test="@rowspan and number(@rowspan) > count(parent::dtbk:tr/following-sibling::dtbk:tr)+1">[tpb95] rowspan attribute value on <name/> element must not be larger than the number of rows left in the table</sch:report>
    </sch:rule>
  </sch:pattern>  
      
</sch:schema>

