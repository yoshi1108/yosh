" Vim global plugin for fx info from yahoo
" Last Change:  2014 Feb 17
" Maintainer:   yosh <yoshi1108@gmail.com>

let s:save_cpo = &cpo
set cpo&vim

let s:home_url = "http://info.finance.yahoo.co.jp/fx/list/"
let s:proxy="false"

" ローカルでSJIS設定
"setl encoding=sjis

" デバッグモード
let s:DEBUG="true"

if ( s:proxy == "true") 
    let $http_proxy   = 'http://proxygate2.nic.nec.co.jp:8080'
else
    let $http_proxy   = ''
endif

if ( s:DEBUG == "false" )
    if exists("g:loaded_webhome")
	finish
    endif
    let g:loaded_webhome = 1
    map <unique> <Leader>h  <Plug>Webhome
endif

command! Webhome :call s:Webhome()

if !executable('curl')
  echohl ErrorMsg | echomsg "require 'curl' command" | echohl None
  finish
endif

function! s:Webhome()
   let s:V = vital#of('vital')
   let s:M = s:V.import('Web.Xml')
   let s:resu = webapi#http#get(s:home_url) 
   let s:result_str = substitute(s:resu.content, "<[^>]*>", " ", "g")
   let s:result_str = substitute(s:result_str, "&nbsp;", "\n", "g")
   if (s:DEBUG != "true")
       :new 'webhome'
   endif
   let s:flag = 'false'
   for line in split(s:result_str, '\n')
	   let line = substitute(line, "^ *", "", "g")
	   let line = substitute(line, " *$", "", "g")
	   if ( line =~# "^月足" ) 
		   let s:flag = "true"
	   	   continue
       endif
	   if ( s:flag == 'false' && !(line =~# "現在の日時"))
	       continue
	   endif
	   if ( line =~# "^FXおすすめ" ) 
	   	   break
       endif
	   if ( line =~# "^※" ) 
	   	   continue
       endif
	   if ( line =~# "^もっと見る" ) 
	   	   continue
       endif
	   if ( line =~# "^情報提供元" ) 
	   	   continue
       endif
	   if ( line =~# "^また、為替レート" ) 
	   	   continue
       endif
	   if ( line =~# "^【ご注意】" ) 
	   	   continue
       endif
	   if ( line == "" ) 
	   	   continue
	   endif
       if (s:DEBUG == "true")
	       echo (line)
	   else
           call append('$', line)
	   endif
   endfor
endfunction

let &cpo = s:save_cpo
unlet s:save_cpo

if (s:DEBUG == "true")
    :Webhome
endif
