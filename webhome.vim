" Vim global plugin for correcting typing mistakes
" Last Change:  2014 Feb 16
" Maintainer:   yosh <yoshi1108@gmail.com>

let s:DEBUG="true"
 
let s:save_cpo = &cpo
set cpo&vim

" ■ DEBUG中はコメントアウト
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

let s:home_url = "http://yoshi1108.web.fc2.com/"
let $http_proxy   = 'http://proxygate2.nic.nec.co.jp:8080'

function! s:Webhome()
   let s:V = vital#of('vital')
   let s:M = s:V.import('Web.Xml')

   " 元のHTMLのまま出力
   let s:resu = webapi#http#get(s:home_url) 
   let s:result_str = substitute(s:resu.content, "<[^>]*>", " ", "g")
   let s:result_str = substitute(s:result_str, "&nbsp;", "\n", "g")
   if (s:DEBUG != "true")
       :new 'webhome'
   endif
   for line in split(s:result_str, '\n')
	   let line = substitute(line, "^ *", "", "g")
	   let line = substitute(line, " *$", "", "g")
	   if ( line == "" ) 
		   continue
	   endif
           if (s:DEBUG != "true")
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
