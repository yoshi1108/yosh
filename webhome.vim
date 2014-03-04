" Vim global plugin for fx info from yahoo
" Last Change:  2014 Mar 04
" Maintainer:   yosh <yoshi1108@gmail.com>

let s:save_cpo = &cpo
set cpo&vim

let s:home_url = "http://info.finance.yahoo.co.jp/fx/list/"
let s:proxy="false"

" ローカルでSJIS設定
"setl encoding=sjis

" デバッグモード
let s:DEBUG="false"

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

" 表示ペア設定
function! s:isDisp(str)
   " 通貨ペア設定。@ をつけると表示されない
   let s:pairList = [
   \'USD/JPY',
   \'EUR/JPY',
   \'AUD/JPY',
   \'GBP/JPY',
   \'NZD/JPY-',
   \'CAD/JPY@',
   \'CHF/JPY@',
   \'ZAR/JPY@',
   \'CNY/JPY@',
   \'EUR/USD',
   \'GBP/USD',
   \'AUD/USD',
   \'NZD/USD@',
   \'HKD/JPY@',
   \'EUR/GBP@',
   \'EUR/AUD',
   \'USD/CHF',
   \'EUR/CHF@',
   \'GBP/CHF@',
   \'AUD/CHF@',
   \'CAD/CHF@',
   \'USD/HKD@'
   \]
   for s:pair in s:pairList
       if ( a:str =~# s:pair )
	       return 1
       endif
   endfor

   return 0 
endfunction

" 通貨ペア変換
function! s:getPair(pair)
   let s:result = substitute(a:pair,   "米ドル",      "USD", "g")
   let s:result = substitute(s:result, "ユーロ",      "EUR", "g")
   let s:result = substitute(s:result, "豪ドル",      "AUD", "g")
   let s:result = substitute(s:result, "英ポンド",    "GBP", "g")
   let s:result = substitute(s:result, "NZドル",      "NZD", "g")
   let s:result = substitute(s:result, "カナダドル",  "CAD", "g")
   let s:result = substitute(s:result, "スイスフラン","CHF", "g")
   let s:result = substitute(s:result, "ランド",      "ZAR", "g")
   let s:result = substitute(s:result, "人民元",      "CNY", "g")
   let s:result = substitute(s:result, "香港ドル",    "HKD", "g")
   let s:result = substitute(s:result, "ドル",        "USD", "g")
   let s:result = substitute(s:result, "円",          "JPY", "g")
   return s:result
endfunction

function! s:Webhome()
   let s:V = vital#of('vital')
   let s:M = s:V.import('Web.Xml')
   let s:resu = webapi#http#get(s:home_url) 
   let s:result_str = substitute(s:resu.content, "<[^>]*>", " ", "g")
   let s:result_str = substitute(s:result_str, "&nbsp;", "\n", "g")
   if (s:DEBUG != "true")
       :vnew10 'webhome'
       let s:buff_flag='true'
       :setlocal buftype=nowrite
       :setlocal noswapfile
       :setlocal bufhidden=wipe
       :setlocal nonumber
       :setlocal nowrap
   endif
   let s:flag = 'false'
   let s:news_flag = 'false'
   " 1行表示バッファ
   let s:one_buff = ""
   for s:line in split(s:result_str, '\n')
	   let s:line = substitute(s:line, "^ *", "", "g")
	   let s:line = substitute(s:line, " *$", "", "g")
	   if ( s:line =~# "^月足" ) 
		   let s:flag = "true"
	   	   continue
       endif
	   if ( s:flag == 'false' && !(s:line =~# "現在の日時"))
	       continue
	   endif
	   if ( s:line =~# "^FXおすすめ" ) 
	   	   break
       endif
	   if ( s:line =~# "^※" ) 
	   	   continue
       endif
	   if ( s:line =~# "^もっと見る" ) 
	   	   continue
       endif
	   if ( s:line =~# "^情報提供元" ) 
	   	   continue
       endif
	   if ( s:line =~# "^また、為替レート" ) 
	   	   continue
       endif
	   if ( s:line =~# "^【ご注意】" ) 
	   	   continue
       endif
	   if ( s:line =~# "^FXニュース" ) 
		   let s:news_flag = 'true'
	   	   continue
       endif
	   if ( s:line == "" ) 
	   	   continue
	   endif
	   if ( s:line =~# "^Bid" ) 
           let s:bid = substitute(s:line, "Bid *","", "g")
		   let s:one_buff = printf("%s %10s", s:one_buff, s:bid)
	   	   continue
	   elseif( s:line =~"^Ask" )
           let s:ask = substitute(s:line, "Ask *","", "g")
		   let s:one_buff = printf("%s %10s", s:one_buff, s:ask)
		   if ( s:isDisp(s:one_buff) != 1 )
		       continue
		   endif
	   elseif( s:line =~"^現在の日時" )
		   let s:one_buff = printf("%s", substitute(s:line, "--.*$", "", "g"))
	   elseif( s:news_flag == 'true' )
		   " ニュースの場合はそのまま表示
	       let s:one_buff = printf("%s", s:line)
       else
		   let s:one_buff = printf("%s", s:line)
	       let s:one_buff = s:getPair(s:one_buff)
		   continue
       endif

       if (s:DEBUG == "true")
	       echo (s:one_buff)
	   else
           call append('$', s:one_buff)
	   endif
	   let s:one_buff = ''
   endfor
endfunction

let &cpo = s:save_cpo
unlet s:save_cpo

if (s:DEBUG == "true")
    :Webhome
endif
