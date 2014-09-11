" Vim global plugin for fx info from yahoo
" Last Change:  2014 Mar 04
" Maintainer:   yosh <yoshi1108@gmail.com>

let s:save_cpo = &cpo
set cpo&vim

let s:home_url = "http://info.finance.yahoo.co.jp/fx/list/"

" ローカルでSJIS設定
"setl encoding=sjis

" デバッグモード
let s:DEBUG='false'

" 通貨ペアを横に並べる数
let s:pairNum=3

" 通貨ペア設定。@ をつけると表示されない
let s:pair_disp_list = [
\'EUR/JPY',
\'EUR/USD',
\'USD/JPY',
\'AUD/JPY',
\'AUD/USD',
\'USD/CHF',
\'GBP/JPY',
\'GBP/USD',
\'NZD/JPY@',
\'CAD/JPY@',
\'CHF/JPY@',
\'ZAR/JPY',
\'CNY/JPY@',
\'NZD/USD@',
\'HKD/JPY@',
\'EUR/GBP',
\'EUR/AUD',
\'EUR/CHF@',
\'GBP/CHF@',
\'AUD/CHF@',
\'CAD/CHF@',
\'USD/HKD@'
\]

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
           
function! s:keta(value)
	let s:value_0 = substitute(a:value, "\.[0-9]*$","", "g")
	let s:value_1 = substitute(a:value, "^[0-9]*\.","", "g")
	while 1
		if ( s:value_1 < 10000 )
			let s:value_1 = s:value_1 . "0" " 桁合わせ
		else
			break
		endif
	endwhile
	return s:value_0 . "." . s:value_1
endfunction

function! s:Webhome()
   let s:get_time = ''  " 取得時刻
   let s:pair_map = {}  " 通貨ペア情報マップ
   let s:news_list = [] " ニュースリスト
   let s:resu = webapi#http#get(s:home_url) 
   let s:result_str = substitute(s:resu.content, "<[^>]*>", " ", "g")
   let s:result_str = substitute(s:result_str, "&nbsp;", "\n", "g")
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
	   if ( s:line =~# "^おすすめコンテンツ" ) 
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
		   let s:bid = s:keta(s:bid)
		   let s:one_buff = printf("%s %10s", s:one_buff, s:bid)
	   	   continue
	   elseif( s:line =~"^Ask" )
           let s:ask = substitute(s:line, "Ask *","", "g")
		   let s:ask = s:keta(s:ask)
		   let s:one_buff = printf("%s %10s", s:one_buff, s:ask)
		   let s:pair = substitute(s:one_buff, " .*$", "", "g")
		   let s:pair_map[s:pair] = s:one_buff " 通貨ペアをキーにASK/BID含んだ文字列を追加
		   continue
	   elseif( s:line =~"^現在の日時" )
		   let s:get_time = printf("%s", substitute(s:line, "--.*$", "", "g"))
	   elseif( s:news_flag == 'true' )
		   " ニュース
		   call add(s:news_list, s:line)
       else
		   let s:one_buff = printf("%s", s:line)
	       let s:one_buff = s:getPair(s:one_buff)
		   continue
       endif
   endfor
   " 情報表示
   echo s:get_time
   echo ''
   let s:tmp_cnt=0
   for s:pair in s:pair_disp_list
       if (has_key(s:pair_map, s:pair))
           echon ('■' . s:pair_map[s:pair] . ' ')
           let s:tmp_cnt += 1
           if ( s:tmp_cnt % s:pairNum == 0)
               echo ''
           endif
       endif
   endfor
   echo '--- FX news ------'
   for s:news in s:news_list
       echo s:news
   endfor
endfunction

if ( s:DEBUG == "true" )
	:Webhome
endif

let &cpo = s:save_cpo
unlet s:save_cpo
